export interface ChannelConfig {
  key: string;
  id: string;
  handle: string;
  name: string;
  nameHi: string;
  description: string;
  descriptionHi: string;
  color: string;
  priority: number;
  isKids: boolean;
}

export interface CategoryConfig {
  slug: string;
  label: string;
  labelHi: string;
  channelKeys: string[];
  keywords: string[];
  priority: number;
}

export interface SectionConfig {
  id: string;
  label: string;
  labelHi: string;
  icon: string;
  color: string;
  priority: number;
}

export const CHANNELS: ChannelConfig[] = [
  {
    key: "pramansagarji",
    id: "", // Set via Firebase config secrets
    handle: "@pramansagarji",
    name: "Muni Pramansagar Ji",
    nameHi: "मुनि प्रमाणसागर जी",
    description: "Discourses, Bhawna Yog, Agam Swadhyay and live events",
    descriptionHi: "प्रवचन, भावना योग, आगम स्वाध्याय और लाइव कार्यक्रम",
    color: "#E8730A",
    priority: 1,
    isKids: false,
  },
  {
    key: "bestofshankasamadhan",
    id: "",
    handle: "@bestofshankasamadhan",
    name: "Best of Shanka Samadhan",
    nameHi: "बेस्ट ऑफ शंका समाधान",
    description: "Selected highlight clips from Shanka Samadhan episodes",
    descriptionHi: "शंका समाधान के चुनिंदा क्लिप्स",
    color: "#C9932A",
    priority: 2,
    isKids: false,
  },
  {
    key: "shankasamadhan",
    id: "",
    handle: "@shankasamadhan",
    name: "Shanka Samadhan",
    nameHi: "शंका समाधान",
    description: "Full episodes of the Q&A series",
    descriptionHi: "पूर्ण प्रश्नोत्तर सत्र",
    color: "#B8860B",
    priority: 3,
    isKids: false,
  },
  {
    key: "jainpathshala",
    id: "",
    handle: "@jainpathshalabypramaniksamooh",
    name: "Jain Pathshala",
    nameHi: "जैन पाठशाला",
    description: "Animated stories and easy Jain concepts for kids",
    descriptionHi: "बच्चों के लिए एनिमेटेड कहानियाँ और जैन शिक्षा",
    color: "#1A4E7A",
    priority: 4,
    isKids: true,
  },
];

export const CATEGORIES: CategoryConfig[] = [
  {
    slug: "discourse",
    label: "Discourses",
    labelHi: "प्रवचन",
    channelKeys: ["pramansagarji"],
    keywords: ["pravachan", "discourse", "प्रवचन", "updesh", "उपदेश"],
    priority: 1,
  },
  {
    slug: "bhawna-yog",
    label: "Bhawna Yog",
    labelHi: "भावना योग",
    channelKeys: ["pramansagarji"],
    keywords: ["bhawna", "bhavna", "yog", "yoga", "भावना"],
    priority: 2,
  },
  {
    slug: "swadhyay",
    label: "Agam Swadhyay",
    labelHi: "आगम स्वाध्याय",
    channelKeys: ["pramansagarji"],
    keywords: ["swadhyay", "agam", "aagam", "स्वाध्याय", "आगम"],
    priority: 3,
  },
  {
    slug: "shanka-clips",
    label: "Q&A Highlights",
    labelHi: "शंका समाधान",
    channelKeys: ["bestofshankasamadhan"],
    keywords: ["shanka", "samadhan", "question", "answer", "शंका"],
    priority: 4,
  },
  {
    slug: "shanka-full",
    label: "Shanka Samadhan (Full)",
    labelHi: "शंका समाधान (पूर्ण)",
    channelKeys: ["shankasamadhan"],
    keywords: ["shanka", "samadhan", "full", "episode"],
    priority: 5,
  },
  {
    slug: "live",
    label: "Live Events",
    labelHi: "लाइव कार्यक्रम",
    channelKeys: ["pramansagarji"],
    keywords: ["live", "event", "paryushan", "लाइव"],
    priority: 6,
  },
  {
    slug: "kids",
    label: "Jain Pathshala",
    labelHi: "जैन पाठशाला",
    channelKeys: ["jainpathshala"],
    keywords: ["kids", "children", "animated", "story", "pathshala", "बच्चे"],
    priority: 7,
  },
];

export const SECTIONS: SectionConfig[] = [
  { id: "pravachan", label: "Discourses", labelHi: "प्रवचन", icon: "om", color: "#E8730A", priority: 1 },
  { id: "shanka-clips", label: "Q&A Highlights", labelHi: "शंका समाधान (क्लिप्स)", icon: "bolt", color: "#C9932A", priority: 2 },
  { id: "shanka-full", label: "Q&A Full Episodes", labelHi: "शंका समाधान (पूर्ण)", icon: "movie", color: "#2D6A4F", priority: 3 },
  { id: "bhawna-yog", label: "Bhawna Yog", labelHi: "भावना योग", icon: "self_improvement", color: "#C9932A", priority: 4 },
  { id: "swadhyay", label: "Agam Swadhyay", labelHi: "आगम स्वाध्याय", icon: "menu_book", color: "#B8860B", priority: 5 },
  { id: "events", label: "Events", labelHi: "कार्यक्रम", icon: "celebration", color: "#FF6B35", priority: 6 },
  { id: "kids", label: "Jain Pathshala", labelHi: "जैन पाठशाला", icon: "star", color: "#1A4E7A", priority: 7 },
];

export const HOME_ROWS = [
  { id: "live", label: "Live Now", labelHi: "अभी लाइव", type: "live", filter: "live", priority: 0, maxItems: 5 },
  { id: "discourse", label: "Latest Discourses", labelHi: "नवीनतम प्रवचन", type: "category", filter: "discourse", priority: 1, maxItems: 12 },
  { id: "bhawna-yog", label: "Bhawna Yog", labelHi: "भावना योग", type: "category", filter: "bhawna-yog", priority: 2, maxItems: 12 },
  { id: "shanka-clips", label: "Shanka Samadhan", labelHi: "शंका समाधान", type: "category", filter: "shanka-clips", priority: 3, maxItems: 12 },
  { id: "swadhyay", label: "Agam Swadhyay", labelHi: "आगम स्वाध्याय", type: "category", filter: "swadhyay", priority: 4, maxItems: 12 },
  { id: "shanka-full", label: "Shanka Samadhan (Full)", labelHi: "शंका समाधान (पूर्ण)", type: "category", filter: "shanka-full", priority: 5, maxItems: 12 },
  { id: "kids", label: "Jain Pathshala", labelHi: "जैन पाठशाला", type: "category", filter: "kids", priority: 6, maxItems: 12 },
];
