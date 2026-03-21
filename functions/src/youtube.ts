const YOUTUBE_BASE = "https://www.googleapis.com/youtube/v3";

// ── Response Interfaces ──

interface YouTubePlaylistsResponse {
  items: Array<{
    id: string;
    snippet: {
      title: string;
      description: string;
      publishedAt: string;
      channelTitle: string;
      thumbnails: {
        medium?: { url: string };
        high?: { url: string };
        maxres?: { url: string };
      };
    };
    contentDetails: {
      itemCount: number;
    };
  }>;
  nextPageToken?: string;
}

interface YouTubePlaylistItemsResponse {
  items: Array<{
    snippet: {
      title: string;
      description: string;
      publishedAt: string;
      position: number;
      resourceId: { videoId: string };
      thumbnails: {
        medium?: { url: string };
        high?: { url: string };
        maxres?: { url: string };
      };
    };
    contentDetails: { videoId: string; videoPublishedAt?: string };
  }>;
  nextPageToken?: string;
}

interface YouTubeVideoDetailsResponse {
  items: Array<{
    id: string;
    snippet: {
      title: string;
      description: string;
      publishedAt: string;
      channelTitle: string;
      tags?: string[];
      thumbnails: {
        medium?: { url: string };
        high?: { url: string };
        maxres?: { url: string };
      };
    };
    contentDetails: { duration: string };
    statistics: { viewCount?: string };
    liveStreamingDetails?: {
      actualStartTime?: string;
      scheduledStartTime?: string;
    };
  }>;
}

interface YouTubeSearchResponse {
  items: Array<{
    id: { videoId: string };
    snippet: {
      title: string;
      liveBroadcastContent: string;
    };
  }>;
}

// ── Data Interfaces ──

export interface PlaylistData {
  id: string;
  title: string;
  description: string;
  thumbnailUrl: string;
  channelKey: string;
  channelName: string;
  videoCount: number;
  publishedAt: string;
  section: string;
  displayOrder: number;
  pinned: boolean;
  visible: boolean;
  lastFetched: FirebaseFirestore.FieldValue | null;
}

export interface VideoData {
  id: string;
  title: string;
  description: string;
  thumbnailUrl: string;
  thumbnailUrlHQ: string;
  channelKey: string;
  channelName: string;
  categorySlug: string;
  playlistId: string;
  playlistTitle: string;
  publishedAt: string;
  duration: string;
  durationFormatted: string;
  viewCount: number;
  viewCountFormatted: string;
  isLive: boolean;
  isUpcoming: boolean;
  tags: string[];
  youtubeUrl: string;
  position: number;
  isShort: boolean;
  durationSec: number;
}

// ── YouTube API Functions ──

/**
 * Fetch all playlists from a YouTube channel.
 * Uses playlists.list with channelId parameter.
 * Paginates to get all playlists (up to maxResults).
 */
export async function fetchChannelPlaylists(
  channelId: string,
  apiKey: string,
  maxResults = 200
): Promise<YouTubePlaylistsResponse["items"]> {
  const allItems: YouTubePlaylistsResponse["items"] = [];
  let pageToken: string | undefined;

  while (allItems.length < maxResults) {
    const url = new URL(`${YOUTUBE_BASE}/playlists`);
    url.searchParams.set("part", "snippet,contentDetails");
    url.searchParams.set("channelId", channelId);
    url.searchParams.set("maxResults", String(Math.min(50, maxResults - allItems.length)));
    url.searchParams.set("key", apiKey);
    if (pageToken) url.searchParams.set("pageToken", pageToken);

    console.log(`Fetching playlists for channel ${channelId} (page: ${pageToken || "first"})`);
    const res = await fetch(url.toString());
    if (!res.ok) {
      const errorBody = await res.text();
      console.error(`YouTube playlists.list error: ${res.status} for channel ${channelId}`, errorBody);
      break;
    }

    const data: YouTubePlaylistsResponse = await res.json();
    if (!data.items || data.items.length === 0) break;

    allItems.push(...data.items);
    pageToken = data.nextPageToken;
    if (!pageToken) break;
  }

  return allItems;
}

/**
 * Fetch video items from a YouTube playlist.
 * Uses playlistItems.list with playlistId parameter.
 * Returns raw playlist items (use fetchVideoDetails for full metadata).
 */
export async function fetchPlaylistItems(
  playlistId: string,
  apiKey: string,
  maxResults = 50
): Promise<YouTubePlaylistItemsResponse["items"]> {
  const allItems: YouTubePlaylistItemsResponse["items"] = [];
  let pageToken: string | undefined;

  while (allItems.length < maxResults) {
    const url = new URL(`${YOUTUBE_BASE}/playlistItems`);
    url.searchParams.set("part", "snippet,contentDetails");
    url.searchParams.set("playlistId", playlistId);
    url.searchParams.set("maxResults", String(Math.min(50, maxResults - allItems.length)));
    url.searchParams.set("key", apiKey);
    if (pageToken) url.searchParams.set("pageToken", pageToken);

    const res = await fetch(url.toString());
    if (!res.ok) {
      const errorBody = await res.text();
      console.error(`YouTube playlistItems.list error: ${res.status} for playlist ${playlistId}`, errorBody);
      break;
    }

    const data: YouTubePlaylistItemsResponse = await res.json();
    if (!data.items || data.items.length === 0) break;

    allItems.push(...data.items);
    pageToken = data.nextPageToken;
    if (!pageToken) break;
  }

  return allItems;
}

/**
 * Fetch videos from a channel's "Uploads" playlist (backward compat).
 */
export async function fetchChannelVideos(
  channelId: string,
  apiKey: string,
  maxResults = 50
): Promise<string[]> {
  const uploadsPlaylistId = "UU" + channelId.slice(2);
  const videoIds: string[] = [];
  let pageToken: string | undefined;

  while (videoIds.length < maxResults) {
    const url = new URL(`${YOUTUBE_BASE}/playlistItems`);
    url.searchParams.set("part", "contentDetails");
    url.searchParams.set("playlistId", uploadsPlaylistId);
    url.searchParams.set("maxResults", String(Math.min(50, maxResults - videoIds.length)));
    url.searchParams.set("key", apiKey);
    if (pageToken) url.searchParams.set("pageToken", pageToken);

    const res = await fetch(url.toString());
    if (!res.ok) {
      const errorBody = await res.text();
      console.error(`YouTube API error: ${res.status} for playlist ${uploadsPlaylistId}`, errorBody);
      break;
    }

    const data: YouTubePlaylistItemsResponse = await res.json();
    for (const item of data.items) {
      videoIds.push(item.contentDetails.videoId);
    }

    pageToken = data.nextPageToken;
    if (!pageToken) break;
  }

  return videoIds;
}

export async function fetchVideoDetails(
  videoIds: string[],
  apiKey: string
): Promise<YouTubeVideoDetailsResponse["items"]> {
  const allItems: YouTubeVideoDetailsResponse["items"] = [];

  // Batch 50 at a time
  for (let i = 0; i < videoIds.length; i += 50) {
    const batch = videoIds.slice(i, i + 50);
    const url = new URL(`${YOUTUBE_BASE}/videos`);
    url.searchParams.set("part", "snippet,contentDetails,statistics,liveStreamingDetails");
    url.searchParams.set("id", batch.join(","));
    url.searchParams.set("key", apiKey);

    const res = await fetch(url.toString());
    if (!res.ok) {
      console.error(`YouTube video details error: ${res.status}`);
      continue;
    }

    const data: YouTubeVideoDetailsResponse = await res.json();
    allItems.push(...data.items);
  }

  return allItems;
}

export async function checkLiveStreams(
  channelId: string,
  apiKey: string
): Promise<YouTubeSearchResponse["items"]> {
  const url = new URL(`${YOUTUBE_BASE}/search`);
  url.searchParams.set("part", "snippet");
  url.searchParams.set("channelId", channelId);
  url.searchParams.set("eventType", "live");
  url.searchParams.set("type", "video");
  url.searchParams.set("key", apiKey);

  const res = await fetch(url.toString());
  if (!res.ok) return [];

  const data: YouTubeSearchResponse = await res.json();
  return data.items;
}

// ── Utility Functions ──

export function parseDuration(iso: string): string {
  const match = iso.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/);
  if (!match) return "0:00";

  const h = parseInt(match[1] || "0");
  const m = parseInt(match[2] || "0");
  const s = parseInt(match[3] || "0");

  if (h > 0) return `${h}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  return `${m}:${String(s).padStart(2, "0")}`;
}

export function getDurationSeconds(iso: string): number {
  const match = iso.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/);
  if (!match) return 0;
  return (parseInt(match[1] || "0") * 3600) +
    (parseInt(match[2] || "0") * 60) +
    parseInt(match[3] || "0");
}

export function formatViewCount(count: number): string {
  if (count >= 10000000) return `${(count / 10000000).toFixed(1)}Cr`;
  if (count >= 100000) return `${(count / 100000).toFixed(1)}L`;
  if (count >= 1000) return `${(count / 1000).toFixed(1)}K`;
  return String(count);
}
