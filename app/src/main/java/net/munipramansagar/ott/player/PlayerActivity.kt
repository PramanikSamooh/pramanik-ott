package net.munipramansagar.ott.player

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsetsController
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

    private lateinit var playerView: PlayerView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var titleOverlay: View
    private var player: ExoPlayer? = null

    private val videoId: String by lazy {
        intent.getStringExtra("videoId") ?: ""
    }

    private val videoTitle: String by lazy {
        intent.getStringExtra("videoTitle") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        }
                        Player.STATE_BUFFERING -> {
                            loadingSpinner.visibility = View.VISIBLE
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

    private fun hideSystemUI() {
        window.insetsController?.let { controller ->
            controller.hide(
                android.view.WindowInsets.Type.statusBars() or
                        android.view.WindowInsets.Type.navigationBars()
            )
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
