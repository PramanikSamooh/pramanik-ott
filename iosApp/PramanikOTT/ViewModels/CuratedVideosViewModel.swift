import SwiftUI
import FirebaseFirestore

@MainActor
class CuratedVideosViewModel: ObservableObject {
    @Published var videos: [VideoItem] = []
    @Published var selectedVideoId: String?
    @Published var isLoading = true

    private let db = Firestore.firestore()

    func loadVideos(collectionId: String) async {
        isLoading = true

        do {
            let snap = try await db.collection("curated/\(collectionId)/videos")
                .order(by: "order")
                .getDocuments()

            videos = snap.documents.map { doc in
                let data = doc.data()
                let videoId = data["videoId"] as? String ?? doc.documentID
                return VideoItem(
                    id: videoId,
                    title: data["title"] as? String ?? "",
                    titleHi: data["titleHi"] as? String ?? "",
                    thumbnailUrl: data["thumbnailUrl"] as? String ?? "https://i.ytimg.com/vi/\(videoId)/hqdefault.jpg"
                )
            }
        } catch {
            print("Error loading curated videos for \(collectionId): \(error)")
        }

        isLoading = false
    }
}
