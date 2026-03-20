import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import { CHANNELS, CATEGORIES, HOME_ROWS } from "./config";
import {
  fetchChannelVideos,
  fetchVideoDetails,
  checkLiveStreams,
  parseDuration,
  getDurationSeconds,
  formatViewCount,
  VideoData,
} from "./youtube";
import { categoriseVideo } from "./categorise";

admin.initializeApp();
const db = admin.firestore();

// Secrets — set via: firebase functions:secrets:set YOUTUBE_API_KEY
const youtubeApiKey = defineSecret("YOUTUBE_API_KEY");
const youtubeChannelPramansagarji = defineSecret("YOUTUBE_CHANNEL_ID_PRAMANSAGARJI");
const youtubeChannelBestofshanka = defineSecret("YOUTUBE_CHANNEL_ID_BESTOFSHANKA");
const youtubeChannelShankasamadhan = defineSecret("YOUTUBE_CHANNEL_ID_SHANKASAMADHAN");
const youtubeChannelJainpathshala = defineSecret("YOUTUBE_CHANNEL_ID_JAINPATHSHALA");

function getChannelId(channelKey: string): string {
  switch (channelKey) {
    case "pramansagarji": return youtubeChannelPramansagarji.value();
    case "bestofshankasamadhan": return youtubeChannelBestofshanka.value();
    case "shankasamadhan": return youtubeChannelShankasamadhan.value();
    case "jainpathshala": return youtubeChannelJainpathshala.value();
    default: return "";
  }
}

// ── Scheduled: Fetch YouTube videos every 2 hours ──
export const fetchYouTubeVideos = onSchedule(
  {
    schedule: "every 2 hours",
    timeZone: "Asia/Kolkata",
    secrets: [
      youtubeApiKey,
      youtubeChannelPramansagarji,
      youtubeChannelBestofshanka,
      youtubeChannelShankasamadhan,
      youtubeChannelJainpathshala,
    ],
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
          // Skip shorts (<=60 seconds)
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
            thumbnailUrl: item.snippet.thumbnails.medium?.url ||
              `https://i.ytimg.com/vi/${item.id}/mqdefault.jpg`,
            thumbnailUrlHQ: item.snippet.thumbnails.high?.url ||
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
            isUpcoming: !!item.liveStreamingDetails?.scheduledStartTime &&
              !item.liveStreamingDetails?.actualStartTime,
            tags: (item.snippet.tags || []).slice(0, 10).map((t) => t.toLowerCase()),
            youtubeUrl: `https://www.youtube.com/watch?v=${item.id}`,
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

// ── Scheduled: Check live status every 5 min (7AM-7PM IST) ──
export const checkLiveStatus = onSchedule(
  {
    schedule: "every 5 minutes",
    timeZone: "Asia/Kolkata",
    secrets: [youtubeApiKey, youtubeChannelPramansagarji],
    memory: "256MiB",
    timeoutSeconds: 60,
  },
  async () => {
    // Only check during 7AM-7PM IST
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

// ── HTTP: Seed initial data (channels, categories, homeRows) ──
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
    res.json({ success: true, message: "Seeded channels, categories, homeRows, live status, config" });
  }
);

// ── HTTP: Manual trigger to fetch videos (for testing) ──
export const triggerFetch = onRequest(
  {
    secrets: [
      youtubeApiKey,
      youtubeChannelPramansagarji,
      youtubeChannelBestofshanka,
      youtubeChannelShankasamadhan,
      youtubeChannelJainpathshala,
    ],
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
        batch.set(db.collection("videos").doc(item.id), {
          id: item.id,
          title: item.snippet.title,
          description: item.snippet.description.slice(0, 500),
          thumbnailUrl: item.snippet.thumbnails.medium?.url ||
            `https://i.ytimg.com/vi/${item.id}/mqdefault.jpg`,
          thumbnailUrlHQ: item.snippet.thumbnails.high?.url ||
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
        }, { merge: true });
        count++;
      }

      if (count > 0) await batch.commit();
      results[channel.key] = count;
    }

    res.json({ success: true, videosWritten: results });
  }
);
