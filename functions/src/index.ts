import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import { CHANNELS, CATEGORIES, HOME_ROWS, SECTIONS } from "./config";
import {
  fetchChannelPlaylists,
  fetchPlaylistItems,
  fetchChannelVideos,
  fetchVideoDetails,
  checkLiveStreams,
  parseDuration,
  getDurationSeconds,
  formatViewCount,
  VideoData,
} from "./youtube";
import { categoriseVideo, categorisePlaylist } from "./categorise";

admin.initializeApp();
const db = admin.firestore();

// Secrets — set via: firebase functions:secrets:set YOUTUBE_API_KEY
const youtubeApiKey = defineSecret("YOUTUBE_API_KEY");
const youtubeChannelPramansagarji = defineSecret("YOUTUBE_CHANNEL_ID_PRAMANSAGARJI");
const youtubeChannelBestofshanka = defineSecret("YOUTUBE_CHANNEL_ID_BESTOFSHANKA");
const youtubeChannelShankasamadhan = defineSecret("YOUTUBE_CHANNEL_ID_SHANKASAMADHAN");
const youtubeChannelJainpathshala = defineSecret("YOUTUBE_CHANNEL_ID_JAINPATHSHALA");

const ALL_SECRETS = [
  youtubeApiKey,
  youtubeChannelPramansagarji,
  youtubeChannelBestofshanka,
  youtubeChannelShankasamadhan,
  youtubeChannelJainpathshala,
];

function getChannelId(channelKey: string): string {
  switch (channelKey) {
    case "pramansagarji": return youtubeChannelPramansagarji.value();
    case "bestofshankasamadhan": return youtubeChannelBestofshanka.value();
    case "shankasamadhan": return youtubeChannelShankasamadhan.value();
    case "jainpathshala": return youtubeChannelJainpathshala.value();
    default: return "";
  }
}

// ── Helper: Process playlists for a single channel ──
async function processChannelPlaylists(
  channelKey: string,
  channelName: string,
  channelId: string,
  apiKey: string
): Promise<number> {
  const playlists = await fetchChannelPlaylists(channelId, apiKey);
  if (playlists.length === 0) return 0;

  // Firestore batch limit is 500 writes; playlists per channel are typically < 100
  const batch = db.batch();
  let count = 0;

  for (const pl of playlists) {
    const section = categorisePlaylist(pl.snippet.title, channelKey);
    const ref = db.collection("playlists").doc(pl.id);

    // Use set with merge so admin-set fields (displayOrder, pinned, visible, section)
    // are not overwritten if they already exist
    const existing = await ref.get();
    const existingData = existing.data();

    const playlistDoc: Record<string, unknown> = {
      id: pl.id,
      title: pl.snippet.title,
      description: pl.snippet.description.slice(0, 500),
      thumbnailUrl:
        pl.snippet.thumbnails.high?.url ||
        pl.snippet.thumbnails.medium?.url ||
        "",
      channelKey,
      channelName,
      videoCount: pl.contentDetails.itemCount,
      publishedAt: pl.snippet.publishedAt,
      lastFetched: admin.firestore.FieldValue.serverTimestamp(),
    };

    // Only set section/displayOrder/pinned/visible if document is new
    if (!existingData) {
      playlistDoc.section = section;
      playlistDoc.displayOrder = 0;
      playlistDoc.pinned = false;
      playlistDoc.visible = true;
    }

    batch.set(ref, playlistDoc, { merge: true });
    count++;
  }

  await batch.commit();
  return count;
}

// ── Helper: Process videos for a single playlist ──
async function processPlaylistVideos(
  playlistId: string,
  playlistTitle: string,
  channelKey: string,
  channelName: string,
  apiKey: string,
  maxVideos = 50
): Promise<number> {
  const items = await fetchPlaylistItems(playlistId, apiKey, maxVideos);
  if (items.length === 0) return 0;

  // Get video IDs for full details
  const videoIds = items
    .map((item) => item.contentDetails.videoId)
    .filter((id) => !!id);

  if (videoIds.length === 0) return 0;

  const details = await fetchVideoDetails(videoIds, apiKey);

  // Build a position map from playlist items
  const positionMap = new Map<string, number>();
  for (const item of items) {
    positionMap.set(item.contentDetails.videoId, item.snippet.position);
  }

  // Write to both /playlists/{playlistId}/videos/{videoId} AND /videos/{videoId}
  // Firestore batch limit is 500; split if needed
  const batches: FirebaseFirestore.WriteBatch[] = [db.batch()];
  let batchIndex = 0;
  let writeCount = 0;
  let totalCount = 0;

  for (const item of details) {
    const durationSec = getDurationSeconds(item.contentDetails.duration);
    const isShort = durationSec <= 60;

    const categorySlug = categoriseVideo(
      item.snippet.title,
      item.snippet.description,
      channelKey
    );

    const viewCount = parseInt(item.statistics.viewCount || "0");
    const position = positionMap.get(item.id) ?? 0;

    const video: VideoData = {
      id: item.id,
      title: item.snippet.title,
      description: item.snippet.description.slice(0, 500),
      thumbnailUrl:
        item.snippet.thumbnails.medium?.url ||
        `https://i.ytimg.com/vi/${item.id}/mqdefault.jpg`,
      thumbnailUrlHQ:
        item.snippet.thumbnails.high?.url ||
        item.snippet.thumbnails.maxres?.url ||
        `https://i.ytimg.com/vi/${item.id}/hqdefault.jpg`,
      channelKey,
      channelName,
      categorySlug,
      playlistId,
      playlistTitle,
      publishedAt: item.snippet.publishedAt,
      duration: item.contentDetails.duration,
      durationFormatted: parseDuration(item.contentDetails.duration),
      viewCount,
      viewCountFormatted: formatViewCount(viewCount),
      isLive: false,
      isUpcoming:
        !!item.liveStreamingDetails?.scheduledStartTime &&
        !item.liveStreamingDetails?.actualStartTime,
      tags: (item.snippet.tags || []).slice(0, 10).map((t) => t.toLowerCase()),
      youtubeUrl: `https://www.youtube.com/watch?v=${item.id}`,
      position,
      isShort,
      durationSec,
    };

    // Check if we need a new batch (each video = 2 writes: subcollection + top-level)
    if (writeCount + 2 > 499) {
      batchIndex++;
      batches.push(db.batch());
      writeCount = 0;
    }

    const currentBatch = batches[batchIndex];

    // Write to subcollection: /playlists/{playlistId}/videos/{videoId}
    currentBatch.set(
      db.collection("playlists").doc(playlistId).collection("videos").doc(item.id),
      video,
      { merge: true }
    );

    // Write to top-level /videos/{videoId} (backward compat + search)
    currentBatch.set(
      db.collection("videos").doc(item.id),
      video,
      { merge: true }
    );

    writeCount += 2;
    totalCount++;
  }

  // Commit all batches
  for (const batch of batches) {
    await batch.commit();
  }

  return totalCount;
}

// ══════════════════════════════════════════════════════════════
// ── Scheduled: Fetch playlists from all channels every 6 hours ──
// ══════════════════════════════════════════════════════════════
export const fetchPlaylists = onSchedule(
  {
    schedule: "every 6 hours",
    timeZone: "Asia/Kolkata",
    secrets: ALL_SECRETS,
    memory: "512MiB",
    timeoutSeconds: 300,
  },
  async () => {
    const apiKey = youtubeApiKey.value();
    const results: Record<string, number> = {};

    for (const channel of CHANNELS) {
      const channelId = getChannelId(channel.key);
      if (!channelId) {
        console.warn(`No channel ID for ${channel.key}, skipping`);
        continue;
      }

      try {
        console.log(`Fetching playlists for ${channel.key}...`);
        const count = await processChannelPlaylists(
          channel.key,
          channel.name,
          channelId,
          apiKey
        );
        results[channel.key] = count;
        console.log(`Stored ${count} playlists for ${channel.key}`);
      } catch (err) {
        console.error(`Error fetching playlists for ${channel.key}:`, err);
      }
    }

    console.log("fetchPlaylists complete:", results);
  }
);

// ══════════════════════════════════════════════════════════════
// ── Scheduled: Fetch videos for all visible playlists every 2 hours ──
// ══════════════════════════════════════════════════════════════
export const fetchPlaylistVideos = onSchedule(
  {
    schedule: "every 6 hours",
    timeZone: "Asia/Kolkata",
    secrets: ALL_SECRETS,
    memory: "1GiB",
    timeoutSeconds: 540,
  },
  async () => {
    const apiKey = youtubeApiKey.value();

    // Get all visible playlists
    const snapshot = await db
      .collection("playlists")
      .where("visible", "==", true)
      .get();

    if (snapshot.empty) {
      console.log("No visible playlists found. Run fetchPlaylists first.");
      return;
    }

    const results: Record<string, number> = {};

    for (const doc of snapshot.docs) {
      const data = doc.data();
      const plId = data.id as string;
      const plTitle = data.title as string;
      const channelKey = data.channelKey as string;
      const channelName = data.channelName as string;

      try {
        const count = await processPlaylistVideos(
          plId,
          plTitle,
          channelKey,
          channelName,
          apiKey
        );
        results[plId] = count;
        console.log(`Fetched ${count} videos for playlist "${plTitle}" (${plId})`);
      } catch (err) {
        console.error(`Error fetching videos for playlist ${plId}:`, err);
      }
    }

    console.log("fetchPlaylistVideos complete:", results);
  }
);

// ══════════════════════════════════════════════════════════════
// ── Scheduled: Backward-compat fetch of channel videos every 2 hours ──
// ══════════════════════════════════════════════════════════════
export const fetchYouTubeVideos = onSchedule(
  {
    schedule: "every 12 hours",
    timeZone: "Asia/Kolkata",
    secrets: ALL_SECRETS,
    memory: "512MiB",
    timeoutSeconds: 300,
  },
  async () => {
    const apiKey = youtubeApiKey.value();
    let totalNew = 0;

    for (const channel of CHANNELS) {
      const channelId = getChannelId(channel.key);
      if (!channelId) {
        console.warn(`No channel ID for ${channel.key}, skipping`);
        continue;
      }

      try {
        console.log(`Fetching videos for ${channel.key}...`);
        const videoIds = await fetchChannelVideos(channelId, apiKey, 50);
        const details = await fetchVideoDetails(videoIds, apiKey);

        const batch = db.batch();
        let batchCount = 0;

        for (const item of details) {
          const durationSec = getDurationSeconds(item.contentDetails.duration);
          const isShort = durationSec <= 60;

          const categorySlug = categoriseVideo(
            item.snippet.title,
            item.snippet.description,
            channel.key
          );

          const viewCount = parseInt(item.statistics.viewCount || "0");
          const video: VideoData = {
            id: item.id,
            title: item.snippet.title,
            description: item.snippet.description.slice(0, 500),
            thumbnailUrl:
              item.snippet.thumbnails.medium?.url ||
              `https://i.ytimg.com/vi/${item.id}/mqdefault.jpg`,
            thumbnailUrlHQ:
              item.snippet.thumbnails.high?.url ||
              item.snippet.thumbnails.maxres?.url ||
              `https://i.ytimg.com/vi/${item.id}/hqdefault.jpg`,
            channelKey: channel.key,
            channelName: channel.name,
            categorySlug,
            playlistId: "",
            playlistTitle: "",
            publishedAt: item.snippet.publishedAt,
            duration: item.contentDetails.duration,
            durationFormatted: parseDuration(item.contentDetails.duration),
            viewCount,
            viewCountFormatted: formatViewCount(viewCount),
            isLive: false,
            isUpcoming:
              !!item.liveStreamingDetails?.scheduledStartTime &&
              !item.liveStreamingDetails?.actualStartTime,
            tags: (item.snippet.tags || []).slice(0, 10).map((t) => t.toLowerCase()),
            youtubeUrl: `https://www.youtube.com/watch?v=${item.id}`,
            position: 0,
            isShort,
            durationSec,
          };

          batch.set(db.collection("videos").doc(item.id), video, { merge: true });
          batchCount++;
        }

        if (batchCount > 0) {
          await batch.commit();
          totalNew += batchCount;
          console.log(`Wrote ${batchCount} videos for ${channel.key}`);
        }
      } catch (err) {
        console.error(`Error fetching ${channel.key}:`, err);
      }
    }

    console.log(`fetchYouTubeVideos complete. Total videos written: ${totalNew}`);
  }
);

// ══════════════════════════════════════════════════════════════
// ── Scheduled: Check live status every 15 min during broadcast hours ──
// Uses RSS feed (0 quota) + videos.list (1 unit per batch of 50)
// Cost: ~4-6 units/day total instead of 4,800 units/day with search.list
// ══════════════════════════════════════════════════════════════════════
export const checkLiveStatus = onSchedule(
  {
    schedule: "every 15 minutes",
    timeZone: "Asia/Kolkata",
    secrets: [youtubeApiKey, youtubeChannelPramansagarji, youtubeChannelJainpathshala],
    memory: "256MiB",
    timeoutSeconds: 60,
  },
  async () => {
    const now = new Date();
    const istHour = (now.getUTCHours() + 5.5) % 24;

    // Only check during 5AM-11PM IST (broadcast window)
    if (istHour < 5 || istHour > 23) {
      await db.collection("live").doc("status").set({
        isLive: false,
        activeStreams: [],
        checkedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      return;
    }

    const apiKey = youtubeApiKey.value();

    // ── Step 1: Check for admin override ──
    // Admin can manually set a live video ID from the admin panel
    try {
      const overrideDoc = await db.collection("live").doc("override").get();
      if (overrideDoc.exists) {
        const override = overrideDoc.data();
        if (override?.active && override?.videoId) {
          // Verify the video is actually live via API (1 unit cost)
          const details = await fetchVideoDetails([override.videoId], apiKey);
          const item = details[0];
          if (item?.liveStreamingDetails?.actualStartTime && !item?.liveStreamingDetails?.actualEndTime) {
            await db.collection("live").doc("status").set({
              isLive: true,
              currentVideoId: override.videoId,
              activeStreams: [{
                videoId: override.videoId,
                channelKey: override.channelKey || "pramansagarji",
                channelName: override.channelName || item.snippet?.channelTitle || "",
                title: item.snippet?.title || override.title || "",
              }],
              upcomingVideos: [],
              checkedAt: admin.firestore.FieldValue.serverTimestamp(),
              source: "admin_override",
            });
            console.log(`Live check: admin override active — ${override.videoId}`);
            return;
          } else {
            // Override video is no longer live — auto-clear
            await db.collection("live").doc("override").update({ active: false });
            console.log(`Live check: admin override cleared — video no longer live`);
          }
        }
      }
    } catch (err) {
      console.error("Error checking admin override:", err);
    }

    // ── Step 2: Check RSS feeds for all channels ──
    const channelsToCheck = [
      { id: youtubeChannelPramansagarji.value(), key: "pramansagarji", name: "Muni Pramansagar Ji" },
      { id: youtubeChannelJainpathshala.value(), key: "jainpathshala", name: "Jain Pathshala" },
    ];

    const recentVideoIds: Array<{ videoId: string; channelKey: string; channelName: string }> = [];

    for (const channel of channelsToCheck) {
      try {
        const rssUrl = `https://www.youtube.com/feeds/videos.xml?channel_id=${channel.id}`;
        const rssRes = await fetch(rssUrl);
        const rssText = await rssRes.text();

        const videoIdMatches = rssText.matchAll(/<yt:videoId>([^<]+)<\/yt:videoId>/g);
        for (const match of videoIdMatches) {
          recentVideoIds.push({
            videoId: match[1],
            channelKey: channel.key,
            channelName: channel.name,
          });
        }
      } catch (err) {
        console.error(`RSS fetch error for ${channel.key}:`, err);
      }
    }

    // ── Step 3: Check if any RSS videos are currently live ──
    const activeStreams: Array<{
      videoId: string;
      channelKey: string;
      channelName: string;
      title: string;
    }> = [];

    if (recentVideoIds.length > 0) {
      try {
        const ids = recentVideoIds.map((v) => v.videoId);
        const details = await fetchVideoDetails(ids, apiKey);

        for (const item of details) {
          const liveDetails = item.liveStreamingDetails;
          if (
            liveDetails &&
            liveDetails.actualStartTime &&
            !liveDetails.actualEndTime
          ) {
            const channelInfo = recentVideoIds.find((v) => v.videoId === item.id);
            activeStreams.push({
              videoId: item.id,
              channelKey: channelInfo?.channelKey || "",
              channelName: channelInfo?.channelName || "",
              title: item.snippet.title,
            });
          }
        }
      } catch (err) {
        console.error("Error checking video live status:", err);
      }
    }

    await db.collection("live").doc("status").set({
      isLive: activeStreams.length > 0,
      currentVideoId: activeStreams[0]?.videoId || "",
      activeStreams,
      upcomingVideos: [],
      checkedAt: admin.firestore.FieldValue.serverTimestamp(),
      source: "rss_auto",
    });

    console.log(`Live check: ${activeStreams.length} active streams (checked ${recentVideoIds.length} recent videos)`);
  }
);

// ══════════════════════════════════════════════════════════════
// ── HTTP: Manual trigger to check live status ──
// ══════════════════════════════════════════════════════════════
export const triggerLiveCheck = onRequest(
  { secrets: [youtubeApiKey, youtubeChannelPramansagarji, youtubeChannelJainpathshala], memory: "256MiB", timeoutSeconds: 60 },
  async (req, res) => {
    const apiKey = youtubeApiKey.value();
    const channelsToCheck = [
      { id: youtubeChannelPramansagarji.value(), key: "pramansagarji", name: "Muni Pramansagar Ji" },
      { id: youtubeChannelJainpathshala.value(), key: "jainpathshala", name: "Jain Pathshala" },
    ];

    const recentVideoIds: Array<{ videoId: string; channelKey: string; channelName: string }> = [];

    for (const channel of channelsToCheck) {
      try {
        const rssUrl = `https://www.youtube.com/feeds/videos.xml?channel_id=${channel.id}`;
        const rssRes = await fetch(rssUrl);
        const rssText = await rssRes.text();
        const videoIdMatches = rssText.matchAll(/<yt:videoId>([^<]+)<\/yt:videoId>/g);
        for (const match of videoIdMatches) {
          recentVideoIds.push({ videoId: match[1], channelKey: channel.key, channelName: channel.name });
        }
      } catch (err) {
        console.error(`RSS error for ${channel.key}:`, err);
      }
    }

    const activeStreams: Array<{ videoId: string; channelKey: string; channelName: string; title: string }> = [];
    const upcomingVideos: Array<{ videoId: string; channelKey: string; channelName: string; title: string; scheduledStart: string }> = [];

    if (recentVideoIds.length > 0) {
      const ids = recentVideoIds.map((v) => v.videoId);
      const details = await fetchVideoDetails(ids, apiKey);
      for (const item of details) {
        const liveDetails = item.liveStreamingDetails;
        if (liveDetails && liveDetails.actualStartTime && !liveDetails.actualEndTime) {
          const channelInfo = recentVideoIds.find((v) => v.videoId === item.id);
          activeStreams.push({
            videoId: item.id,
            channelKey: channelInfo?.channelKey || "",
            channelName: channelInfo?.channelName || "",
            title: item.snippet?.title || "",
          });
        } else if (liveDetails && liveDetails.scheduledStartTime && !liveDetails.actualStartTime) {
          const channelInfo = recentVideoIds.find((v) => v.videoId === item.id);
          upcomingVideos.push({
            videoId: item.id,
            channelKey: channelInfo?.channelKey || "",
            channelName: channelInfo?.channelName || "",
            title: item.snippet?.title || "",
            scheduledStart: liveDetails.scheduledStartTime,
          });
        }
      }
    }

    await db.collection("live").doc("status").set({
      isLive: activeStreams.length > 0,
      currentVideoId: activeStreams[0]?.videoId || "",
      activeStreams,
      upcomingVideos,
      checkedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    res.json({
      success: true,
      rssVideosFound: recentVideoIds.length,
      activeStreams: activeStreams.length,
      upcomingVideos: upcomingVideos.length,
      streams: activeStreams,
      upcoming: upcomingVideos,
    });
  }
);

// ══════════════════════════════════════════════════════════════
// ── HTTP: Set live override from admin panel ──
// Admin can manually set a video as live (for unlisted/private streams)
// ══════════════════════════════════════════════════════════════
export const setLiveOverride = onRequest(
  { secrets: [youtubeApiKey], memory: "256MiB", timeoutSeconds: 30, cors: ["https://admin.munipramansagar.net", "https://www.munipramansagar.net", "http://localhost:3000"] },
  async (req, res) => {
    const { videoId, channelKey, channelName, active } = req.body || {};

    if (active === false) {
      // Clear override
      await db.collection("live").doc("override").set({ active: false });
      await db.collection("live").doc("status").set({
        isLive: false,
        currentVideoId: "",
        activeStreams: [],
        upcomingVideos: [],
        checkedAt: admin.firestore.FieldValue.serverTimestamp(),
        source: "admin_cleared",
      });
      res.json({ success: true, message: "Live override cleared" });
      return;
    }

    if (!videoId) {
      res.status(400).json({ error: "videoId is required" });
      return;
    }

    // Verify video exists and get its details
    const apiKey = youtubeApiKey.value();
    const details = await fetchVideoDetails([videoId], apiKey);
    const item = details[0];

    if (!item) {
      res.status(404).json({ error: "Video not found" });
      return;
    }

    const isLive = !!(item.liveStreamingDetails?.actualStartTime && !item.liveStreamingDetails?.actualEndTime);
    const title = item.snippet?.title || "";

    // Set override
    await db.collection("live").doc("override").set({
      active: true,
      videoId,
      channelKey: channelKey || "pramansagarji",
      channelName: channelName || item.snippet?.channelTitle || "",
      title,
      setAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    // Also update live status immediately
    await db.collection("live").doc("status").set({
      isLive: isLive,
      currentVideoId: videoId,
      activeStreams: isLive ? [{
        videoId,
        channelKey: channelKey || "pramansagarji",
        channelName: channelName || item.snippet?.channelTitle || "",
        title,
      }] : [],
      upcomingVideos: [],
      checkedAt: admin.firestore.FieldValue.serverTimestamp(),
      source: "admin_override",
    });

    res.json({
      success: true,
      videoId,
      title,
      isCurrentlyLive: isLive,
      message: isLive ? "Video is LIVE — override set" : "Video found but not currently live — override set anyway",
    });
  }
);

// ══════════════════════════════════════════════════════════════
// ── HTTP: Manual trigger to fetch all playlists ──
// ══════════════════════════════════════════════════════════════
export const triggerFetchPlaylists = onRequest(
  {
    secrets: ALL_SECRETS,
    memory: "512MiB",
    timeoutSeconds: 300,
  },
  async (req, res) => {
    const apiKey = youtubeApiKey.value();
    const results: Record<string, number> = {};

    for (const channel of CHANNELS) {
      const channelId = getChannelId(channel.key);
      if (!channelId) continue;

      try {
        const count = await processChannelPlaylists(
          channel.key,
          channel.name,
          channelId,
          apiKey
        );
        results[channel.key] = count;
      } catch (err) {
        console.error(`Error fetching playlists for ${channel.key}:`, err);
        results[channel.key] = -1;
      }
    }

    res.json({ success: true, playlistsWritten: results });
  }
);

// ══════════════════════════════════════════════════════════════
// ── HTTP: Manual trigger to fetch videos for all visible playlists ──
// ══════════════════════════════════════════════════════════════
export const triggerFetchVideos = onRequest(
  {
    secrets: ALL_SECRETS,
    memory: "1GiB",
    timeoutSeconds: 540,
  },
  async (req, res) => {
    const apiKey = youtubeApiKey.value();

    const snapshot = await db
      .collection("playlists")
      .where("visible", "==", true)
      .get();

    if (snapshot.empty) {
      res.json({ success: false, message: "No visible playlists found. Run triggerFetchPlaylists first." });
      return;
    }

    const results: Record<string, number> = {};

    for (const doc of snapshot.docs) {
      const data = doc.data();
      const plId = data.id as string;
      const plTitle = data.title as string;
      const channelKey = data.channelKey as string;
      const channelName = data.channelName as string;

      try {
        const count = await processPlaylistVideos(
          plId,
          plTitle,
          channelKey,
          channelName,
          apiKey
        );
        results[plId] = count;
      } catch (err) {
        console.error(`Error fetching videos for playlist ${plId}:`, err);
        results[plId] = -1;
      }
    }

    res.json({ success: true, videosWritten: results });
  }
);

// ══════════════════════════════════════════════════════════════
// ── HTTP: Seed sections collection with default sections ──
// ══════════════════════════════════════════════════════════════
export const seedSections = onRequest(
  {},
  async (req, res) => {
    const batch = db.batch();

    for (const section of SECTIONS) {
      batch.set(db.collection("sections").doc(section.id), {
        ...section,
        visible: true,
      });
    }

    await batch.commit();
    res.json({ success: true, message: `Seeded ${SECTIONS.length} sections`, sections: SECTIONS.map((s) => s.id) });
  }
);

// ══════════════════════════════════════════════════════════════
// ── HTTP: Seed initial data (channels, categories, homeRows) ──
// ══════════════════════════════════════════════════════════════
export const seedData = onRequest(
  {
    secrets: [
      youtubeChannelPramansagarji,
      youtubeChannelBestofshanka,
      youtubeChannelShankasamadhan,
      youtubeChannelJainpathshala,
    ],
  },
  async (req, res) => {
    const batch = db.batch();

    // Seed channels
    for (const ch of CHANNELS) {
      const channelId = getChannelId(ch.key);
      batch.set(db.collection("channels").doc(ch.key), {
        ...ch,
        id: channelId,
      });
    }

    // Seed categories
    for (const cat of CATEGORIES) {
      batch.set(db.collection("categories").doc(cat.slug), cat);
    }

    // Seed home rows
    for (const row of HOME_ROWS) {
      batch.set(db.collection("homeRows").doc(row.id), row);
    }

    // Seed sections
    for (const section of SECTIONS) {
      batch.set(db.collection("sections").doc(section.id), {
        ...section,
        visible: true,
      });
    }

    // Initialize live status
    batch.set(db.collection("live").doc("status"), {
      isLive: false,
      currentVideoId: "",
      upcomingVideos: [],
    });

    // App config
    batch.set(db.collection("config").doc("app"), {
      appVersion: "2.0.0",
      maintenanceMode: false,
    });

    await batch.commit();
    res.json({ success: true, message: "Seeded channels, categories, homeRows, sections, live status, config" });
  }
);

// ══════════════════════════════════════════════════════════════
// ── HTTP: Seed sample announcements ──
// ══════════════════════════════════════════════════════════════
export const seedAnnouncements = onRequest({}, async (req, res) => {
  const batch = db.batch();

  const announcements = [
    {
      id: "event1",
      title: "Siddh Chakra Maha Mandal Vidhan 2025",
      titleHi: "श्री सिद्धचक्र महामंडल विधान 2025",
      body: "Register now for this grand spiritual event",
      bodyHi: "इस भव्य आध्यात्मिक कार्यक्रम के लिए अभी पंजीकरण करें",
      type: "event",
      imageUrl: "",
      actionUrl: "https://www.munipramansagar.net/siddhchakra-mandal-vidhan-2025/",
      actionLabel: "Register Now →",
      actionLabelHi: "अभी पंजीकरण करें →",
      priority: 1,
      active: true,
    },
    {
      id: "quote1",
      title: "आज का विचार",
      titleHi: "आज का विचार",
      body: "जो अपने आत्मा को जानता है, वह सब कुछ जानता है।",
      bodyHi: "जो अपने आत्मा को जानता है, वह सब कुछ जानता है।",
      type: "quote",
      imageUrl: "",
      actionUrl: "",
      actionLabel: "",
      actionLabelHi: "",
      priority: 2,
      active: true,
    },
    {
      id: "whatsapp1",
      title: "Join WhatsApp Channel",
      titleHi: "WhatsApp चैनल से जुड़ें",
      body: "Get daily updates on discourses and events",
      bodyHi: "प्रवचन और कार्यक्रमों की दैनिक अपडेट पाएं",
      type: "whatsapp",
      imageUrl: "",
      actionUrl: "https://whatsapp.com/channel/0029VaAVh4jCBtxHGKm1g21U",
      actionLabel: "Follow →",
      actionLabelHi: "फॉलो करें →",
      priority: 3,
      active: true,
    },
    {
      id: "book1",
      title: "भावना योग - फील टू हील",
      titleHi: "भावना योग - फील टू हील",
      body: "New book available — Order now",
      bodyHi: "नई पुस्तक उपलब्ध — अभी ऑर्डर करें",
      type: "notification",
      imageUrl: "",
      actionUrl: "https://www.munipramansagar.net/book-store/",
      actionLabel: "Order Book →",
      actionLabelHi: "पुस्तक ऑर्डर करें →",
      priority: 4,
      active: true,
    },
  ];

  for (const a of announcements) {
    batch.set(db.collection("announcements").doc(a.id), a);
  }

  await batch.commit();
  res.json({ success: true, message: `Seeded ${announcements.length} announcements` });
});

// ══════════════════════════════════════════════════════════════
// ── HTTP: Seed sample pathshala data ──
// ══════════════════════════════════════════════════════════════
export const seedPathshala = onRequest({}, async (req, res) => {
  const batch = db.batch();

  const teachers = [
    { id: "teacher1", name: "Sapna Ji", nameHi: "सपना जी", language: "hindi", photoUrl: "", active: true },
    { id: "teacher2", name: "Priya Ji", nameHi: "प्रिया जी", language: "english", photoUrl: "", active: true },
    { id: "teacher3", name: "Manisha Ji", nameHi: "मनीषा जी", language: "marathi", photoUrl: "", active: true },
  ];

  for (const t of teachers) {
    batch.set(db.collection("pathshala").doc("teachers").collection("items").doc(t.id), t);
  }

  const classes = [
    { id: "class1", title: "Jain Tattva Gyan", titleHi: "जैन तत्व ज्ञान", description: "Basic Jain philosophy", descriptionHi: "जैन दर्शन मूलभूत", teacherId: "teacher1", teacherName: "Sapna Ji", language: "hindi", dayOfWeek: [1, 3, 5], time: "17:00", duration: 45, youtubeLink: "", active: true, ageGroup: "8-14", level: "beginner" },
    { id: "class2", title: "Jain Stories & Values", titleHi: "जैन कहानियां और मूल्य", description: "Interactive stories", descriptionHi: "रोचक कहानियां", teacherId: "teacher2", teacherName: "Priya Ji", language: "english", dayOfWeek: [2, 4], time: "18:00", duration: 30, youtubeLink: "", active: true, ageGroup: "6-12", level: "beginner" },
    { id: "class3", title: "Navkar Mantra & Poojan", titleHi: "नवकार मंत्र और पूजन", description: "Daily prayers", descriptionHi: "दैनिक प्रार्थना और पूजन", teacherId: "teacher1", teacherName: "Sapna Ji", language: "hindi", dayOfWeek: [0, 1, 2, 3, 4, 5, 6], time: "09:00", duration: 60, youtubeLink: "https://www.youtube.com/watch?v=r-Z6NOQ9wZo", active: true, ageGroup: "all", level: "all" },
    { id: "class4", title: "Samaysar Adhyayan", titleHi: "समयसार अध्ययन", description: "Advanced study", descriptionHi: "समयसार गहन अध्ययन", teacherId: "teacher3", teacherName: "Manisha Ji", language: "marathi", dayOfWeek: [6], time: "10:00", duration: 45, youtubeLink: "", active: true, ageGroup: "14+", level: "advanced" },
  ];

  for (const c of classes) {
    batch.set(db.collection("pathshala").doc("classes").collection("items").doc(c.id), c);
  }

  await batch.commit();
  res.json({ success: true, message: `Seeded ${teachers.length} teachers + ${classes.length} classes` });
});

// ══════════════════════════════════════════════════════════════
// ── HTTP: Manual trigger to fetch videos (backward compat) ──
// ══════════════════════════════════════════════════════════════
export const triggerFetch = onRequest(
  {
    secrets: ALL_SECRETS,
    memory: "512MiB",
    timeoutSeconds: 300,
  },
  async (req, res) => {
    const apiKey = youtubeApiKey.value();
    const results: Record<string, number> = {};

    for (const channel of CHANNELS) {
      const channelId = getChannelId(channel.key);
      if (!channelId) continue;

      const videoIds = await fetchChannelVideos(channelId, apiKey, 50);
      const details = await fetchVideoDetails(videoIds, apiKey);

      const batch = db.batch();
      let count = 0;

      for (const item of details) {
        const durationSec = getDurationSeconds(item.contentDetails.duration);
        const isShort = durationSec <= 60;

        const categorySlug = categoriseVideo(
          item.snippet.title,
          item.snippet.description,
          channel.key
        );

        const viewCount = parseInt(item.statistics.viewCount || "0");
        batch.set(
          db.collection("videos").doc(item.id),
          {
            id: item.id,
            title: item.snippet.title,
            description: item.snippet.description.slice(0, 500),
            thumbnailUrl:
              item.snippet.thumbnails.medium?.url ||
              `https://i.ytimg.com/vi/${item.id}/mqdefault.jpg`,
            thumbnailUrlHQ:
              item.snippet.thumbnails.high?.url ||
              `https://i.ytimg.com/vi/${item.id}/hqdefault.jpg`,
            channelKey: channel.key,
            channelName: channel.name,
            categorySlug,
            playlistId: "",
            playlistTitle: "",
            publishedAt: item.snippet.publishedAt,
            duration: item.contentDetails.duration,
            durationFormatted: parseDuration(item.contentDetails.duration),
            viewCount,
            viewCountFormatted: formatViewCount(viewCount),
            isLive: false,
            isUpcoming: false,
            tags: (item.snippet.tags || []).slice(0, 10).map((t) => t.toLowerCase()),
            youtubeUrl: `https://www.youtube.com/watch?v=${item.id}`,
            position: 0,
            isShort,
            durationSec,
          },
          { merge: true }
        );
        count++;
      }

      if (count > 0) await batch.commit();
      results[channel.key] = count;
    }

    res.json({ success: true, videosWritten: results });
  }
);
