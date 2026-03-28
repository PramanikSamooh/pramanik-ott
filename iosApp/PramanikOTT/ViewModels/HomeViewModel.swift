import SwiftUI
import FirebaseFirestore

struct VideoItem: Identifiable {
    let id: String
    var title: String = ""
    var titleHi: String = ""
    var thumbnailUrl: String = ""
    var channelName: String = ""
    var publishedAt: String = ""
    var durationFormatted: String = ""

    func getTitle(isHindi: Bool) -> String {
        isHindi && !titleHi.isEmpty ? titleHi : title
    }
}

struct PlaylistItem: Identifiable {
    let id: String
    var title: String = ""
    var titleHi: String = ""
    var thumbnailUrl: String = ""
    var videos: [VideoItem] = []
    var pinned: Bool = false
}

struct SectionItem: Identifiable {
    let id: String
    var label: String = ""
    var labelHi: String = ""
    var playlists: [PlaylistItem] = []
}

struct LiveStatus {
    var isLive: Bool = false
    var videoId: String = ""
    var title: String = ""
}

struct WatchHistoryItem: Identifiable {
    let id: String
    var videoId: String
    var title: String = ""
    var thumbnailUrl: String = ""
    var resumePositionMs: Int64 = 0
    var totalDurationMs: Int64 = 0
    var bookmarked: Bool = false

    func toVideoItem() -> VideoItem {
        VideoItem(id: videoId, title: title, thumbnailUrl: thumbnailUrl.isEmpty ? "https://i.ytimg.com/vi/\(videoId)/hqdefault.jpg" : thumbnailUrl)
    }
}

@MainActor
class HomeViewModel: ObservableObject {
    @Published var heroBannerVideos: [VideoItem] = []
    @Published var liveStatus = LiveStatus()
    @Published var sections: [SectionItem] = []
    @Published var continueWatching: [WatchHistoryItem] = []
    @Published var bookmarkedVideos: [WatchHistoryItem] = []
    @Published var isLoading = true
    @Published var selectedVideoId: String?
    @Published var navigateToSection: String?
    @Published var navigateToPlaylist: String?

    private let db = Firestore.firestore()

    func loadHome() async {
        isLoading = true

        async let sectionsTask = loadSections()
        async let heroTask = loadHeroBanner()
        async let liveTask = checkLive()

        let (loadedSections, hero, live) = await (sectionsTask, heroTask, liveTask)

        sections = loadedSections
        heroBannerVideos = hero
        liveStatus = live
        isLoading = false
    }

    private func loadSections() async -> [SectionItem] {
        do {
            let snap = try await db.collection("sections")
                .whereField("visible", isEqualTo: true)
                .order(by: "displayOrder")
                .getDocuments()

            var result: [SectionItem] = []
            for doc in snap.documents {
                let data = doc.data()
                var section = SectionItem(
                    id: doc.documentID,
                    label: data["label"] as? String ?? "",
                    labelHi: data["labelHi"] as? String ?? ""
                )

                // Load playlists for this section
                let plSnap = try await db.collection("playlists")
                    .whereField("section", isEqualTo: doc.documentID)
                    .limit(to: 20)
                    .getDocuments()

                section.playlists = plSnap.documents
                    .filter { ($0.data()["visible"] as? Bool) ?? true }
                    .map { plDoc in
                        let plData = plDoc.data()
                        return PlaylistItem(
                            id: plDoc.documentID,
                            title: plData["title"] as? String ?? "",
                            titleHi: plData["titleHi"] as? String ?? "",
                            thumbnailUrl: plData["thumbnailUrl"] as? String ?? "",
                            pinned: plData["pinned"] as? Bool ?? false
                        )
                    }

                if !section.playlists.isEmpty {
                    result.append(section)
                }
            }
            return result
        } catch {
            print("Error loading sections: \(error)")
            return []
        }
    }

    private func loadHeroBanner() async -> [VideoItem] {
        do {
            let doc = try await db.collection("config").document("hero").getDocument()
            if let videos = doc.data()?["videos"] as? [[String: Any]] {
                return videos.compactMap { map in
                    guard let videoId = map["videoId"] as? String else { return nil }
                    return VideoItem(
                        id: videoId,
                        title: map["title"] as? String ?? "",
                        thumbnailUrl: map["thumbnailUrl"] as? String ?? "https://i.ytimg.com/vi/\(videoId)/maxresdefault.jpg"
                    )
                }
            }
        } catch {
            print("Error loading hero: \(error)")
        }
        return []
    }

    private func checkLive() async -> LiveStatus {
        do {
            let doc = try await db.collection("config").document("live").getDocument()
            let data = doc.data() ?? [:]
            return LiveStatus(
                isLive: data["isLive"] as? Bool ?? false,
                videoId: data["videoId"] as? String ?? "",
                title: data["title"] as? String ?? ""
            )
        } catch {
            return LiveStatus()
        }
    }
}
