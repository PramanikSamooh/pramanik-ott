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
    // Skip shorts (<=60 seconds)
    if (durationSec <= 60) continue;

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
    schedule: "every 2 hours",
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
    schedule: "every 2 hours",
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
          if (durationSec <= 60) continue;

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
// ── Scheduled: Check live status every 5 min (7AM-7PM IST) ──
// ══════════════════════════════════════════════════════════════
export const checkLiveStatus = onSchedule(
  {
    schedule: "every 5 minutes",
    timeZone: "Asia/Kolkata",
    secrets: [youtubeApiKey, youtubeChannelPramansagarji],
    memory: "256MiB",
    timeoutSeconds: 60,
  },
  async () => {
    const now = new Date();
    const istHour = (now.getUTCHours() + 5.5) % 24;
    if (istHour < 7 || istHour > 19) {
      await db.collection("live").doc("status").set({
        isLive: false,
        currentVideoId: "",
        upcomingVideos: [],
        checkedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      return;
    }

    const apiKey = youtubeApiKey.value();
    const channelId = youtubeChannelPramansagarji.value();

    try {
      const liveItems = await checkLiveStreams(channelId, apiKey);
      const isLive = liveItems.length > 0;
      const currentVideoId = liveItems[0]?.id?.videoId || "";

      await db.collection("live").doc("status").set({
        isLive,
        currentVideoId,
        upcomingVideos: [],
        checkedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      console.log(`Live check: ${isLive ? "LIVE - " + currentVideoId : "Not live"}`);
    } catch (err) {
      console.error("Live check error:", err);
    }
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
        if (durationSec <= 60) continue;

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
