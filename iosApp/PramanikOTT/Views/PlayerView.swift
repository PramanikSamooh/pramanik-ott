import SwiftUI
import WebKit

struct PlayerView: View {
    let videoId: String
    let isHindi: Bool
    @Environment(\.dismiss) private var dismiss
    @State private var isBookmarked = false
    @State private var showSpeedPicker = false
    @State private var playbackSpeed = 1.0

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            VStack(spacing: 0) {
                // Top bar
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark")
                            .foregroundColor(.white)
                            .font(.title2)
                    }
                    Spacer()
                    // Bookmark
                    Button(action: { isBookmarked.toggle() }) {
                        Image(systemName: isBookmarked ? "star.fill" : "star")
                            .foregroundColor(isBookmarked ? Color("Saffron") : .white)
                            .font(.title2)
                    }
                    // Speed
                    Button(action: { showSpeedPicker.toggle() }) {
                        Image(systemName: "speedometer")
                            .foregroundColor(.white)
                            .font(.title2)
                    }
                }
                .padding()

                // YouTube Player (WKWebView)
                YouTubePlayerView(videoId: videoId)
                    .ignoresSafeArea()

                Spacer()
            }
        }
        .confirmationDialog("Playback Speed", isPresented: $showSpeedPicker) {
            ForEach([0.5, 0.75, 1.0, 1.25, 1.5, 2.0], id: \.self) { speed in
                Button("\(speed, specifier: "%.2g")x") {
                    playbackSpeed = speed
                }
            }
        }
    }
}

struct YouTubePlayerView: UIViewRepresentable {
    let videoId: String

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []

        let webView = WKWebView(frame: .zero, configuration: config)
        webView.scrollView.isScrollEnabled = false
        webView.backgroundColor = .black
        webView.isOpaque = false

        let html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
            <style>
                body { margin: 0; background: #000; }
                #player { width: 100vw; height: 100vh; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script src="https://www.youtube.com/iframe_api"></script>
            <script>
                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        videoId: '\(videoId)',
                        playerVars: {
                            autoplay: 1,
                            playsinline: 1,
                            modestbranding: 1,
                            rel: 0,
                            showinfo: 0,
                            controls: 1
                        },
                        events: {
                            onReady: function(e) { e.target.playVideo(); }
                        }
                    });
                }
            </script>
        </body>
        </html>
        """
        webView.loadHTMLString(html, baseURL: URL(string: "https://www.youtube.com"))
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}
