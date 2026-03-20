import { CATEGORIES } from "./config";

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
