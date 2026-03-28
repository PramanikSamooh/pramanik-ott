import SwiftUI

struct HeroBannerView: View {
    let videos: [VideoItem]
    let isHindi: Bool
    let onVideoTap: (VideoItem) -> Void
    @State private var currentPage = 0

    var body: some View {
        TabView(selection: $currentPage) {
            ForEach(Array(videos.enumerated()), id: \.element.id) { index, video in
                Button(action: { onVideoTap(video) }) {
                    ZStack(alignment: .bottomLeading) {
                        AsyncImage(url: URL(string: "https://i.ytimg.com/vi/\(video.id)/maxresdefault.jpg")) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Rectangle().fill(Color.gray.opacity(0.3))
                        }
                        .clipped()

                        // Gradient overlay
                        LinearGradient(colors: [.clear, .black.opacity(0.7)], startPoint: .center, endPoint: .bottom)

                        // Title
                        Text(video.getTitle(isHindi: isHindi))
                            .font(.headline)
                            .foregroundColor(.white)
                            .padding()
                    }
                }
                .buttonStyle(.plain)
                .tag(index)
            }
        }
        .tabViewStyle(.page(indexDisplayMode: .automatic))
    }
}

struct LiveBannerView: View {
    let status: LiveStatus
    let isHindi: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack {
                // Pulsing red dot
                Circle()
                    .fill(.red)
                    .frame(width: 10, height: 10)

                Text("LIVE")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(.red)

                Text(status.title)
                    .font(.subheadline)
                    .foregroundColor(.white)
                    .lineLimit(1)

                Spacer()

                Image(systemName: "play.fill")
                    .foregroundColor(Color("Saffron"))
            }
            .padding(12)
            .background(Color.red.opacity(0.15))
            .cornerRadius(12)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.red.opacity(0.3), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

struct CategoryGridView: View {
    let isHindi: Bool
    let onNavigate: (String) -> Void

    struct Category: Identifiable {
        let id: String
        let labelEn: String
        let labelHi: String
        let icon: String
        let color: Color
        let route: String
    }

    let categories = [
        Category(id: "bhawna", labelEn: "Bhawna Yog", labelHi: "भावना योग", icon: "ic_bhawna_yog", color: .green, route: "section/bhawna-yog"),
        Category(id: "pravachan", labelEn: "Pravachan", labelHi: "प्रवचन", icon: "ic_pravachan", color: Color("Saffron"), route: "section/pravachan"),
        Category(id: "shanka", labelEn: "Shanka Samadhan", labelHi: "शंका समाधान", icon: "ic_shanka_samadhan", color: .yellow, route: "section/shanka-clips"),
        Category(id: "swadhyay", labelEn: "Swadhyay", labelHi: "स्वाध्याय", icon: "ic_swadhyay", color: .purple, route: "section/swadhyay"),
    ]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                ForEach(categories) { cat in
                    Button(action: { onNavigate(cat.route) }) {
                        VStack(spacing: 10) {
                            Circle()
                                .fill(cat.color.opacity(0.15))
                                .frame(width: 56, height: 56)
                                .overlay {
                                    Image(cat.icon)
                                        .renderingMode(.template)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 28, height: 28)
                                        .foregroundColor(cat.color)
                                }

                            Text(isHindi ? cat.labelHi : cat.labelEn)
                                .font(.caption2)
                                .fontWeight(.medium)
                                .foregroundColor(.primary)
                                .multilineTextAlignment(.center)
                                .lineLimit(2)
                        }
                        .frame(width: 80)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
        }
    }
}
