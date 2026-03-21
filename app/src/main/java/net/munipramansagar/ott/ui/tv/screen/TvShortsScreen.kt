package net.munipramansagar.ott.ui.tv.screen

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.viewmodel.ShortsViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun TvShortsScreen(
    viewModel: ShortsViewModel = hiltViewModel(),
    isHindi: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val streamUrls by viewModel.streamUrls.collectAsState()
    var currentIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Saffron)
                }
            }

            uiState.shorts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isHindi) "अभी कोई शॉर्ट्स उपलब्ध नहीं" else "No shorts available yet",
                            style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
                        )
                    }
                }
            }

            else -> {
                val shorts = uiState.shorts
                val video = shorts[currentIndex]
                val streamUrl = streamUrls[video.id]
                var isPlayerReady by remember { mutableStateOf(false) }

                // Request extraction
                LaunchedEffect(video.id) {
                    viewModel.extractStreamUrl(video.id)
                    // Preload next
                    if (currentIndex + 1 < shorts.size) {
                        viewModel.extractStreamUrl(shorts[currentIndex + 1].id)
                    }
                }

                // Create ExoPlayer
                val exoPlayer = remember(video.id) {
                    ExoPlayer.Builder(context).build().apply {
                        repeatMode = Player.REPEAT_MODE_ONE
                        playWhenReady = true
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_READY) {
                                    isPlayerReady = true
                                }
                            }
                        })
                    }
                }

                LaunchedEffect(streamUrl) {
                    if (streamUrl != null && streamUrl.isNotEmpty()) {
                        exoPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
                        exoPlayer.prepare()
                    }
                }

                DisposableEffect(video.id) {
                    onDispose { exoPlayer.release() }
                }

                // D-pad handler
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when (event.key) {
                                    Key.DirectionRight -> {
                                        if (currentIndex < shorts.size - 1) {
                                            currentIndex++
                                            viewModel.onPageChanged(currentIndex)
                                        }
                                        true
                                    }
                                    Key.DirectionLeft -> {
                                        if (currentIndex > 0) {
                                            currentIndex--
                                            viewModel.onPageChanged(currentIndex)
                                        }
                                        true
                                    }
                                    Key.Enter, Key.DirectionCenter -> {
                                        if (exoPlayer.isPlaying) exoPlayer.pause()
                                        else exoPlayer.play()
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                        .focusable()
                        .onFocusChanged { }
                ) {
                    // Video player
                    if (streamUrl != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    setBackgroundColor(android.graphics.Color.BLACK)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Loading
                    if (streamUrl == null || !isPlayerReady) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Saffron,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Navigation hints
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (currentIndex > 0) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }

                        if (currentIndex < shorts.size - 1) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Bottom overlay with title
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f),
                                        Color.Black.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                    ) {
                        Column {
                            Text(
                                text = video.title,
                                style = PramanikTvTheme.typography.titleLarge.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = video.channelName,
                                    style = PramanikTvTheme.typography.bodyMedium.copy(
                                        color = TextGray
                                    )
                                )

                                if (video.durationFormatted.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = video.durationFormatted,
                                            style = PramanikTvTheme.typography.labelMedium.copy(
                                                color = TextWhite
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = "${currentIndex + 1} / ${shorts.size}",
                                    style = PramanikTvTheme.typography.bodyMedium.copy(
                                        color = TextGray
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
