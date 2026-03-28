import SwiftUI

struct VideoCardView: View {
    let video: VideoItem
    let isHindi: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(alignment: .leading, spacing: 0) {
                // Thumbnail
                AsyncImage(url: URL(string: video.thumbnailUrl.isEmpty ? "https://i.ytimg.com/vi/\(video.id)/mqdefault.jpg" : video.thumbnailUrl)) { image in
                    image
                        .resizable()
                        .aspectRatio(16/9, contentMode: .fill)
                } placeholder: {
                    Rectangle()
                        .fill(Color.gray.opacity(0.2))
                        .aspectRatio(16/9, contentMode: .fill)
                        .overlay {
                            ProgressView()
                                .tint(Color("Saffron"))
                        }
                }
                .clipped()
                .cornerRadius(8, corners: [.topLeft, .topRight])

                // Title
                VStack(alignment: .leading, spacing: 4) {
                    Text(video.getTitle(isHindi: isHindi))
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundColor(.primary)
                        .lineLimit(2)

                    if !video.channelName.isEmpty {
                        Text(video.channelName)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
                .padding(8)
            }
            .background(Color("CardBackground"))
            .cornerRadius(8)
        }
        .buttonStyle(.plain)
    }
}

struct VideoRowView: View {
    let videos: [VideoItem]
    let isHindi: Bool
    let onVideoTap: (VideoItem) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(videos) { video in
                    VideoCardView(video: video, isHindi: isHindi) {
                        onVideoTap(video)
                    }
                    .frame(width: 180)
                }
            }
            .padding(.horizontal, 16)
        }
    }
}

struct PlaylistRowView: View {
    let playlists: [PlaylistItem]
    let isHindi: Bool
    let onPlaylistTap: (PlaylistItem) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(playlists) { playlist in
                    Button(action: { onPlaylistTap(playlist) }) {
                        VStack(alignment: .leading) {
                            AsyncImage(url: URL(string: playlist.thumbnailUrl)) { image in
                                image.resizable().aspectRatio(16/9, contentMode: .fill)
                            } placeholder: {
                                Rectangle().fill(Color.gray.opacity(0.2)).aspectRatio(16/9, contentMode: .fill)
                            }
                            .clipped()
                            .cornerRadius(8)

                            Text(isHindi && !playlist.titleHi.isEmpty ? playlist.titleHi : playlist.title)
                                .font(.caption)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)
                                .lineLimit(1)
                        }
                        .frame(width: 160)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
        }
    }
}

// Corner radius extension for specific corners
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}
