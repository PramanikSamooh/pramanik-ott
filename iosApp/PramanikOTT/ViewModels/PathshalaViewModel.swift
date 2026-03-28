import SwiftUI
import FirebaseFirestore

struct PathshalaClass: Identifiable {
    let id: String
    var title: String = ""
    var titleHi: String = ""
    var time: String = ""
    var dayOfWeek: Int = 0
    var youtubeLink: String = ""

    func getTitle(isHindi: Bool) -> String {
        isHindi && !titleHi.isEmpty ? titleHi : title
    }
}

@MainActor
class PathshalaViewModel: ObservableObject {
    @Published var todaysClasses: [PathshalaClass] = []
    @Published var classesByDay: [Int: [PathshalaClass]] = [:]
    @Published var isLoading = true

    private let db = Firestore.firestore()

    func loadClasses() async {
        isLoading = true

        do {
            let snap = try await db.collection("pathshala/classes/items")
                .getDocuments()

            var allClasses: [PathshalaClass] = []
            for doc in snap.documents {
                let data = doc.data()
                let cls = PathshalaClass(
                    id: doc.documentID,
                    title: data["title"] as? String ?? "",
                    titleHi: data["titleHi"] as? String ?? "",
                    time: data["time"] as? String ?? "",
                    dayOfWeek: data["dayOfWeek"] as? Int ?? 0,
                    youtubeLink: data["youtubeLink"] as? String ?? ""
                )
                allClasses.append(cls)
            }

            // Group by day of week
            classesByDay = Dictionary(grouping: allClasses, by: { $0.dayOfWeek })

            // Filter today's classes
            let todayIndex = Calendar.current.component(.weekday, from: Date()) - 1 // 0=Sun
            todaysClasses = classesByDay[todayIndex] ?? []

        } catch {
            print("Error loading pathshala classes: \(error)")
        }

        isLoading = false
    }
}
