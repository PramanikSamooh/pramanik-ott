package net.munipramansagar.ott.ui.tv.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import android.view.KeyEvent
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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.ui.tv.theme.DarkBg
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.SaffronLight
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TvHeroBanner(
    videos: List<Video>,
    onPlayClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    var isBannerFocused by remember { mutableStateOf(false) }

    // Auto-advance every 6 seconds (pause when focused for manual control)
    LaunchedEffect(videos.size, isBannerFocused) {
        if (!isBannerFocused) {
            while (true) {
                delay(6000)
                currentIndex = (currentIndex + 1) % videos.size
            }
        }
    }

    val currentVideo = videos[currentIndex]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .onFocusChanged { isBannerFocused = it.hasFocus }
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            currentIndex = if (currentIndex > 0) currentIndex - 1 else videos.size - 1
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            currentIndex = (currentIndex + 1) % videos.size
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        // ── Background image with crossfade + subtle scale ──
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                (fadeIn(tween(700)) + scaleIn(tween(700), initialScale = 1.04f))
                    .togetherWith(fadeOut(tween(500)) + scaleOut(tween(500), targetScale = 0.98f))
            },
            label = "heroBanner"
        ) { index ->
            val video = videos[index]
            AsyncImage(
                model = video.thumbnailUrlHQ.ifEmpty { video.thumbnailUrl },
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Gradient overlays ──

        // Bottom-to-top gradient (covers bottom 70%)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DarkBg.copy(alpha = 0.4f),
                            DarkBg.copy(alpha = 0.85f),
                            DarkBg
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Left-to-right gradient (covers left 40%)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DarkBg.copy(alpha = 0.9f),
                            DarkBg.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 900f
                    )
                )
        )

        // ── Content (bottom-left) ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 48.dp, end = 320.dp)
        ) {
            // Playlist / category pill
            val pillLabel = currentVideo.playlistTitle.ifBlank { currentVideo.categorySlug }
            if (pillLabel.isNotBlank()) {
                Text(
                    text = pillLabel,
                    style = PramanikTvTheme.typography.labelMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier
                        .background(Saffron.copy(alpha = 0.9f), PramanikTvTheme.shapes.pill)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Title
            Text(
                text = currentVideo.title,
                style = PramanikTvTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    lineHeight = 42.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Meta row: channel + duration + views
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentVideo.channelName.isNotEmpty()) {
                    Text(
                        text = currentVideo.channelName,
                        style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
                    )
                }
                if (currentVideo.durationFormatted.isNotEmpty()) {
                    Text(
                        text = "\u2022",
                        style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
                    )
                    Text(
                        text = currentVideo.durationFormatted,
                        style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
                    )
                }
                if (currentVideo.viewCountFormatted.isNotEmpty()) {
                    Text(
                        text = "\u2022",
                        style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
                    )
                    Text(
                        text = currentVideo.viewCountFormatted,
                        style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Play button – filled saffron
                Button(
                    onClick = { onPlayClick(currentVideo) },
                    colors = ButtonDefaults.colors(
                        containerColor = Saffron,
                        contentColor = TextWhite,
                        focusedContainerColor = SaffronLight,
                        focusedContentColor = Color.White
                    ),
                    shape = ButtonDefaults.shape(
                        shape = PramanikTvTheme.shapes.button
                    )
                ) {
                    Text(
                        text = "\u25B6  Play",
                        style = PramanikTvTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }

            }
        }

        // ── Left/Right arrow indicators (visible when focused) ──
        if (videos.size > 1) {
            // Left arrow
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
                    .size(36.dp)
                    .background(
                        Color.Black.copy(alpha = if (isBannerFocused) 0.5f else 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = Color.White.copy(alpha = if (isBannerFocused) 0.9f else 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
            // Right arrow
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .background(
                        Color.Black.copy(alpha = if (isBannerFocused) 0.5f else 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = Color.White.copy(alpha = if (isBannerFocused) 0.9f else 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ── Dot / pill indicators ──
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            videos.forEachIndexed { index, _ ->
                val isActive = index == currentIndex
                val pillWidth by animateDpAsState(
                    targetValue = if (isActive) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
                    label = "pillWidth"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(pillWidth)
                        .height(4.dp)
                        .clip(PramanikTvTheme.shapes.pill)
                        .background(
                            if (isActive) Saffron
                            else Color.White.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}
