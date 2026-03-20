import { CATEGORIES } from "./config";

/**
 * Categorise a video based on title, description, and channel key.
 * Used for the legacy /videos collection (backward compat).
 */
export function categoriseVideo(
  title: string,
  description: string,
  channelKey: string
): string {
  const text = `${title} ${description}`.toLowerCase();

  // Check for live content first
  if (text.includes("live") || text.includes("लाइव")) {
    return "live";
  }

  // For non-pramansagarji channels, use channel default mapping
  const channelDefaults: Record<string, string> = {
    bestofshankasamadhan: "shanka-clips",
    shankasamadhan: "shanka-full",
    jainpathshala: "kids",
  };

  if (channelDefaults[channelKey]) {
    return channelDefaults[channelKey];
  }

  // For pramansagarji — keyword matching
  let bestSlug = "discourse"; // default
  let bestScore = 0;

  for (const cat of CATEGORIES) {
    if (!cat.channelKeys.includes(channelKey)) continue;

    let score = 0;
    for (const keyword of cat.keywords) {
      if (text.includes(keyword.toLowerCase())) {
        score++;
      }
    }

    if (score > bestScore) {
      bestScore = score;
      bestSlug = cat.slug;
    }
  }

  return bestSlug;
}

/**
 * Auto-assign a playlist to a section based on channel key and playlist title.
 * Returns the section ID string.
 */
export function categorisePlaylist(
  playlistTitle: string,
  channelKey: string
): string {
  const title = playlistTitle.toLowerCase();

  // Channel-level defaults (high confidence)
  if (channelKey === "bestofshankasamadhan") return "shanka-clips";
  if (channelKey === "shankasamadhan") return "shanka-full";
  if (channelKey === "jainpathshala") return "kids";

  // For pramansagarji, use keyword matching on playlist title
  // Check specific keywords before falling back to default
  if (title.includes("bhawna") || title.includes("bhavna") || title.includes("भावना")) {
    return "bhawna-yog";
  }
  if (title.includes("swadhyay") || title.includes("agam") || title.includes("aagam") ||
      title.includes("स्वाध्याय") || title.includes("आगम")) {
    return "swadhyay";
  }
  if (title.includes("vidhaan") || title.includes("vidhan") || title.includes("panchkalyanak") ||
      title.includes("mahotsav") || title.includes("event") || title.includes("कार्यक्रम") ||
      title.includes("विधान") || title.includes("पंचकल्याणक") || title.includes("महोत्सव")) {
    return "events";
  }
  if (title.includes("shanka") || title.includes("samadhan") || title.includes("शंका")) {
    return "shanka-clips";
  }
  if (title.includes("live") || title.includes("लाइव")) {
    return "events";
  }

  // Default for pramansagarji: pravachan
  return "pravachan";
}
