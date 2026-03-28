import SwiftUI

struct HomeView: View {
    let isHindi: Bool
    @StateObject private var viewModel = HomeViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Hero Banner
                if !viewModel.heroBannerVideos.isEmpty {
                    HeroBannerView(videos: viewModel.heroBannerVideos, isHindi: isHindi) { video in
                        viewModel.selectedVideoId = video.id
                    }
                    .frame(height: 220)
                }

                // Live Banner
                if viewModel.liveStatus.isLive {
                    LiveBannerView(status: viewModel.liveStatus, isHindi: isHindi) {
                        viewModel.selectedVideoId = viewModel.liveStatus.videoId
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 12)
                }

                // Category Grid
                CategoryGridView(isHindi: isHindi) { route in
                    viewModel.navigateToSection = route
                }
                .padding(.top, 16)

                // Continue Watching
                if !viewModel.continueWatching.isEmpty {
                    SectionHeader(title: isHindi ? "जारी रखें" : "Continue Watching")
                    VideoRowView(videos: viewModel.continueWatching.map { $0.toVideoItem() }, isHindi: isHindi) { video in
                        viewModel.selectedVideoId = video.id
                    }
                }

                // Saved Videos
                if !viewModel.bookmarkedVideos.isEmpty {
                    SectionHeader(title: isHindi ? "सहेजे गए वीडियो ★" : "Saved Videos ★")
                    VideoRowView(videos: viewModel.bookmarkedVideos.map { $0.toVideoItem() }, isHindi: isHindi) { video in
                        viewModel.selectedVideoId = video.id
                    }
                }

                // Sections
                ForEach(viewModel.sections, id: \.id) { section in
                    SectionHeader(title: isHindi ? section.labelHi : section.label)
                    PlaylistRowView(playlists: section.playlists, isHindi: isHindi) { playlist in
                        viewModel.navigateToPlaylist = playlist.id
                    }
                }

                Spacer(minLength: 80)
            }
        }
        .background(Color("Background"))
        .navigationTitle(isHindi ? "प्रमाणिक" : "Pramanik")
        .navigationBarTitleDisplayMode(.inline)
        .fullScreenCover(isPresented: Binding(
            get: { viewModel.selectedVideoId != nil },
            set: { if !$0 { viewModel.selectedVideoId = nil } }
        )) {
            if let videoId = viewModel.selectedVideoId {
                PlayerView(videoId: videoId, isHindi: isHindi)
            }
        }
        .task {
            await viewModel.loadHome()
        }
    }
}

struct SectionHeader: View {
    let title: String

    var body: some View {
        HStack {
            Text(title)
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.top, 16)
        .padding(.bottom, 8)
    }
}
