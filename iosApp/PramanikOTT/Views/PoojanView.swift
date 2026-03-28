import SwiftUI

struct PoojanTab: Identifiable {
    let id: String
    let labelEn: String
    let labelHi: String
    let collectionId: String
}

let poojanTabs = [
    PoojanTab(id: "nitya", labelEn: "Nitya Poojan", labelHi: "नित्य पूजन", collectionId: "curated_nitya_poojan"),
    PoojanTab(id: "path", labelEn: "Path", labelHi: "पाठ", collectionId: "curated_path"),
    PoojanTab(id: "stotra", labelEn: "Stotra", labelHi: "स्तोत्र", collectionId: "curated_stotra"),
    PoojanTab(id: "bhajan", labelEn: "Bhajan", labelHi: "भजन", collectionId: "curated_bhajan"),
    PoojanTab(id: "granth", labelEn: "Granth Vachan", labelHi: "ग्रंथ वाचन", collectionId: "curated_granth_vachan"),
]

struct PoojanView: View {
    let isHindi: Bool
    @State private var selectedTab = 0

    var body: some View {
        VStack(spacing: 0) {
            // Tab bar
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 16) {
                    ForEach(Array(poojanTabs.enumerated()), id: \.element.id) { index, tab in
                        Button(action: { selectedTab = index }) {
                            Text(isHindi ? tab.labelHi : tab.labelEn)
                                .font(.subheadline)
                                .fontWeight(selectedTab == index ? .bold : .regular)
                                .foregroundColor(selectedTab == index ? Color("Saffron") : .secondary)
                                .padding(.vertical, 8)
                                .overlay(alignment: .bottom) {
                                    if selectedTab == index {
                                        Rectangle()
                                            .fill(Color("Saffron"))
                                            .frame(height: 3)
                                    }
                                }
                        }
                    }
                }
                .padding(.horizontal, 16)
            }

            // Content
            TabView(selection: $selectedTab) {
                ForEach(Array(poojanTabs.enumerated()), id: \.element.id) { index, tab in
                    CuratedVideosView(collectionId: tab.collectionId, isHindi: isHindi)
                        .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
        .navigationTitle(isHindi ? "पूजन और पाठ" : "Poojan & Path")
    }
}

struct CuratedVideosView: View {
    let collectionId: String
    let isHindi: Bool
    @StateObject private var viewModel: CuratedVideosViewModel

    init(collectionId: String, isHindi: Bool) {
        self.collectionId = collectionId
        self.isHindi = isHindi
        _viewModel = StateObject(wrappedValue: CuratedVideosViewModel())
    }

    var body: some View {
        ScrollView {
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                ForEach(viewModel.videos, id: \.id) { video in
                    VideoCardView(video: video, isHindi: isHindi) {
                        viewModel.selectedVideoId = video.id
                    }
                }
            }
            .padding(16)
        }
        .task {
            await viewModel.loadVideos(collectionId: collectionId)
        }
        .fullScreenCover(isPresented: Binding(
            get: { viewModel.selectedVideoId != nil },
            set: { if !$0 { viewModel.selectedVideoId = nil } }
        )) {
            if let videoId = viewModel.selectedVideoId {
                PlayerView(videoId: videoId, isHindi: isHindi)
            }
        }
    }
}
