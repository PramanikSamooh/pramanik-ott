package net.munipramansagar.ott.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.munipramansagar.ott.R
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.VideoStream

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    @javax.inject.Inject
    lateinit var watchHistoryRepository: net.munipramansagar.ott.data.repository.WatchHistoryRepository

    private lateinit var playerView: PlayerView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var titleOverlay: View
    private var player: ExoPlayer? = null
    private var webView: WebView? = null
    private var isWebViewMode = false

    private val videoId: String by lazy { intent.getStringExtra("videoId") ?: "" }
    private val videoTitle: String by lazy { intent.getStringExtra("videoTitle") ?: "" }
    private val videoTitleHi: String by lazy { intent.getStringExtra("videoTitleHi") ?: "" }
    private val videoThumbnail: String by lazy { intent.getStringExtra("videoThumbnail") ?: "" }
    private val channelName: String by lazy { intent.getStringExtra("channelName") ?: "" }
    private val durationFormatted: String by lazy { intent.getStringExtra("durationFormatted") ?: "" }
    private val playlistId: String by lazy { intent.getStringExtra("playlistId") ?: "" }
    private val playlistTitle: String by lazy { intent.getStringExtra("playlistTitle") ?: "" }
    private val sectionId: String by lazy { intent.getStringExtra("sectionId") ?: "" }
    private val playlistIndex: Int by lazy { intent.getIntExtra("playlistIndex", -1) }

    // For periodic progress saving
    private val progressSaveRunnable = object : Runnable {
        override fun run() {
            saveCurrentProgress()
            playerView.postDelayed(this, 10_000) // Save every 10 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PlayerActivity", "onCreate videoId=$videoId")
        try {
            setContentView(R.layout.activity_player)

            playerView = findViewById(R.id.player_view)
            loadingSpinner = findViewById(R.id.loading_spinner)
            titleOverlay = findViewById(R.id.title_overlay)
            titleText = findViewById(R.id.title_text)

            val backButton = findViewById<ImageButton>(R.id.back_button)
            backButton.setOnClickListener { finish() }

            titleText.text = videoTitle

            if (videoId.isEmpty()) {
                finish()
                return
            }

            hideSystemUI()

            initPlayer()
            extractAndPlay()
        } catch (e: Throwable) {
            Log.e("PlayerActivity", "onCreate crashed", e)
            openInYouTubeApp()
        }
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            loadingSpinner.visibility = View.GONE
                            // Auto-hide title after 3 seconds
                            titleOverlay.postDelayed({
                                titleOverlay.animate().alpha(0f).setDuration(500).start()
                            }, 3000)
                            // Resume from saved position
                            resumeFromSavedPosition()
                            // Start periodic progress saving
                            playerView.removeCallbacks(progressSaveRunnable)
                            playerView.postDelayed(progressSaveRunnable, 10_000)
                        }
                        Player.STATE_BUFFERING -> {
                            loadingSpinner.visibility = View.VISIBLE
                        }
                        Player.STATE_ENDED -> {
                            // Save as completed and try auto-next
                            saveCurrentProgress()
                            autoPlayNext()
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("PlayerActivity", "Playback error", error)
                    Toast.makeText(
                        this@PlayerActivity,
                        "Playback error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
        playerView.player = player
    }

    private var hasResumed = false
    private fun resumeFromSavedPosition() {
        if (hasResumed) return
        hasResumed = true
        lifecycleScope.launch {
            val position = watchHistoryRepository.getResumePosition(videoId)
            if (position > 5000) { // Only resume if > 5 seconds in
                player?.seekTo(position)
                Toast.makeText(
                    this@PlayerActivity,
                    "Resuming from where you left off",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveCurrentProgress() {
        val p = player ?: return
        val pos = p.currentPosition
        val dur = p.duration.coerceAtLeast(0)
        if (pos < 3000) return // Don't save if watched less than 3 seconds
        lifecycleScope.launch {
            watchHistoryRepository.saveProgress(
                videoId = videoId,
                title = videoTitle,
                titleHi = videoTitleHi,
                thumbnailUrl = videoThumbnail.ifEmpty { "https://i.ytimg.com/vi/$videoId/mqdefault.jpg" },
                channelName = channelName,
                durationFormatted = durationFormatted,
                playlistId = playlistId,
                playlistTitle = playlistTitle,
                sectionId = sectionId,
                positionMs = pos,
                totalDurationMs = dur,
                playlistIndex = playlistIndex
            )
        }
    }

    private fun autoPlayNext() {
        if (playlistId.isEmpty()) return
        lifecycleScope.launch {
            val next = watchHistoryRepository.getNextInPlaylist(playlistId)
            if (next != null && next.videoId != videoId) {
                // Start PlayerActivity with the next video
                val nextIntent = android.content.Intent(this@PlayerActivity, PlayerActivity::class.java).apply {
                    putExtra("videoId", next.videoId)
                    putExtra("videoTitle", next.title)
                    putExtra("videoTitleHi", next.titleHi)
                    putExtra("videoThumbnail", next.thumbnailUrl)
                    putExtra("playlistId", next.playlistId)
                    putExtra("playlistTitle", next.playlistTitle)
                    putExtra("sectionId", next.sectionId)
                    putExtra("playlistIndex", next.playlistIndex)
                }
                startActivity(nextIntent)
                finish()
            }
        }
    }

    private fun extractAndPlay() {
        loadingSpinner.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val streamUrl = withContext(Dispatchers.IO) {
                    // Initialize NewPipe if not already done
                    try {
                        NewPipe.init(DownloaderImpl.getInstance())
                    } catch (_: Exception) {
                        // Already initialized
                    }

                    val url = "https://www.youtube.com/watch?v=$videoId"
                    val info = StreamInfo.getInfo(
                        ServiceList.YouTube,
                        url
                    )

                    // Get best video stream (prefer 720p or lower for mobile)
                    val videoStreams = info.videoStreams
                        .filter { !it.isVideoOnly }
                        .sortedByDescending { it.resolution?.replace("p", "")?.toIntOrNull() ?: 0 }

                    // Pick 720p or best available under 1080p
                    val bestStream = videoStreams.firstOrNull {
                        val res = it.resolution?.replace("p", "")?.toIntOrNull() ?: 0
                        res in 360..720
                    } ?: videoStreams.firstOrNull()

                    bestStream?.content ?: info.hlsUrl ?: ""
                }

                if (streamUrl.isNotEmpty()) {
                    val mediaItem = MediaItem.fromUri(streamUrl)
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                } else {
                    Toast.makeText(
                        this@PlayerActivity,
                        "Could not load video stream",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingSpinner.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("PlayerActivity", "Stream extraction failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PlayerActivity,
                        "Failed to load video: ${e.message?.take(80)}",
                        Toast.LENGTH_LONG
                    ).show()
                    loadingSpinner.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewPlayer() {
        isWebViewMode = true
        // Hide ExoPlayer view, show WebView instead
        playerView.visibility = View.GONE
        loadingSpinner.visibility = View.GONE

        val wv = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Add WebView to the root layout (behind title overlay)
        val root = findViewById<FrameLayout>(android.R.id.content)
            .getChildAt(0) as FrameLayout
        root.addView(wv, 0)

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36"
        }

        wv.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val host = request?.url?.host ?: return true
                if (host.endsWith("youtube.com") || host.endsWith("ytimg.com") ||
                    host.endsWith("googleapis.com") || host.endsWith("googlevideo.com") ||
                    host.endsWith("google.com")
                ) {
                    // Block navigation to other videos
                    val path = request.url?.path ?: ""
                    if (path.startsWith("/watch")) {
                        val clickedId = request.url?.getQueryParameter("v") ?: ""
                        if (clickedId.isNotEmpty() && clickedId != videoId) return true
                    }
                    return false
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Hide YouTube UI — show only video player fullscreen
                val js = """
                    (function(){
                        var s=document.createElement('style');
                        s.textContent=`
                            ytm-mobile-topbar-renderer,header,.mobile-topbar-header,
                            ytm-pivot-bar-renderer,#bottom-bar,footer,
                            ytm-item-section-renderer,[section-identifier="related-items"],
                            [section-identifier="comments-entry-point"],
                            ytm-comments-entry-point-header-renderer,
                            ytm-comment-section-renderer,
                            .related-chips-slot-wrapper,
                            ytm-chip-cloud-renderer,
                            .slim-video-metadata-header,
                            .slim-owner-icon-and-title,
                            ytm-slim-video-action-bar-renderer,
                            #below-the-player,#secondary,#menu,
                            .watch-below-the-player,
                            ytm-engagement-panel-section-list-renderer
                            {display:none!important}
                            .player-container,.html5-video-player,
                            #player-container-id{
                                position:fixed!important;top:0!important;left:0!important;
                                width:100vw!important;height:100vh!important;z-index:9999!important
                            }
                            video{width:100vw!important;height:100vh!important;object-fit:contain!important}
                            body{overflow:hidden!important;background:#000!important}
                        `;
                        document.head.appendChild(s);
                    })();
                """.trimIndent()
                view?.evaluateJavascript(js, null)

                // Auto-hide title overlay
                titleOverlay.postDelayed({
                    titleOverlay.animate().alpha(0f).setDuration(500).start()
                }, 3000)
            }
        }

        wv.webChromeClient = WebChromeClient()
        wv.loadUrl("https://m.youtube.com/watch?v=$videoId")
        webView = wv
    }

    private fun openInYouTubeApp() {
        try {
            // Use https URL — works better on Android TV YouTube app
            // FLAG_ACTIVITY_NEW_TASK + CLEAR_TOP ensures new video loads
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.youtube.com/watch?v=$videoId")
            ).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        } catch (_: Exception) {
            // Last resort: vnd.youtube deep link
            try {
                val appIntent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("vnd.youtube:$videoId")
                )
                startActivity(appIntent)
            } catch (_: Exception) { }
        }
        finish()
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.let { controller ->
                controller.hide(
                    android.view.WindowInsets.Type.statusBars() or
                            android.view.WindowInsets.Type.navigationBars()
                )
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                player?.let {
                    if (it.isPlaying) it.pause() else it.play()
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                player?.seekTo((player?.currentPosition ?: 0) - 10000)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                player?.seekTo((player?.currentPosition ?: 0) + 10000)
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        saveCurrentProgress()
        playerView.removeCallbacks(progressSaveRunnable)
        if (isWebViewMode) webView?.onPause() else player?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (isWebViewMode) webView?.onResume() else player?.play()
    }

    override fun onDestroy() {
        saveCurrentProgress()
        playerView.removeCallbacks(progressSaveRunnable)
        if (isWebViewMode) {
            webView?.destroy()
            webView = null
        } else {
            player?.release()
            player = null
        }
        super.onDestroy()
    }
}
