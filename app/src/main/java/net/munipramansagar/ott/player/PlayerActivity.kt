package net.munipramansagar.ott.player

import android.annotation.SuppressLint
import android.os.Bundle
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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import net.munipramansagar.ott.R
import net.munipramansagar.ott.util.Constants

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var titleOverlay: View
    private lateinit var titleText: TextView
    private lateinit var fullscreenContainer: FrameLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private val videoId: String by lazy {
        intent.getStringExtra("videoId") ?: ""
    }

    private val videoTitle: String by lazy {
        intent.getStringExtra("videoTitle") ?: ""
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        fullscreenContainer = findViewById(R.id.fullscreen_container)
        titleOverlay = findViewById(R.id.title_overlay)
        titleText = findViewById(R.id.title_text)
        webView = findViewById(R.id.player_webview)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener { finish() }

        titleText.text = videoTitle

        setupWebView()
        loadVideo()
        hideSystemUI()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Only allow YouTube domains
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val host = request?.url?.host ?: return true
                return !isAllowedDomain(host)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                customView = view
                customViewCallback = callback
                fullscreenContainer.addView(view)
                fullscreenContainer.visibility = View.VISIBLE
                webView.visibility = View.GONE
            }

            override fun onHideCustomView() {
                fullscreenContainer.removeAllViews()
                fullscreenContainer.visibility = View.GONE
                webView.visibility = View.VISIBLE
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
            }
        }
    }

    private fun isAllowedDomain(host: String): Boolean {
        val allowed = listOf(
            "youtube-nocookie.com",
            "youtube.com",
            "www.youtube.com",
            "www.youtube-nocookie.com",
            "ytimg.com",
            "i.ytimg.com",
            "googleapis.com"
        )
        return allowed.any { host.endsWith(it) }
    }

    private fun loadVideo() {
        if (videoId.isEmpty()) {
            finish()
            return
        }

        val embedUrl = "${Constants.YOUTUBE_EMBED_BASE}$videoId" +
                "?autoplay=1&rel=0&modestbranding=1&playsinline=1&enablejsapi=1"
        webView.loadUrl(embedUrl)
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
        // TV D-pad controls
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                // Toggle play/pause
                webView.evaluateJavascript(
                    "document.querySelector('video')?.paused ? " +
                            "document.querySelector('video')?.play() : " +
                            "document.querySelector('video')?.pause()", null
                )
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                webView.evaluateJavascript(
                    "document.querySelector('video').currentTime -= 10", null
                )
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                webView.evaluateJavascript(
                    "document.querySelector('video').currentTime += 10", null
                )
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (customView != null) {
                    webView.webChromeClient?.onHideCustomView()
                    return true
                }
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
