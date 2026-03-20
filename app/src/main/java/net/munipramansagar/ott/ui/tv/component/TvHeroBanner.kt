package net.munipramansagar.ott.ui.tv.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.ui.tv.theme.DarkBackground
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
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

    // Auto-advance every 5 seconds
    LaunchedEffect(videos.size) {
        while (true) {
            delay(5000)
            currentIndex = (currentIndex + 1) % videos.size
        }
    }

    val currentVideo = videos[currentIndex]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        // Background image
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "heroBanner"
        ) { index ->
            val video = videos[index]
            AsyncImage(
                model = video.thumbnailUrlHQ.ifEmpty { video.thumbnailUrl },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gradient overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkBackground.copy(alpha = 0.3f),
                            DarkBackground.copy(alpha = 0.7f),
                            DarkBackground
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DarkBackground.copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 800f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 40.dp, end = 300.dp)
        ) {
            Text(
                text = currentVideo.title,
                style = PramanikTvTheme.typography.displayMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentVideo.channelName.isNotEmpty()) {
                    Text(
                        text = currentVideo.channelName,
                        style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
                    )
                }
                if (currentVideo.durationFormatted.isNotEmpty()) {
                    Text(
                        text = " \u2022 ${currentVideo.durationFormatted}",
                        style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onPlayClick(currentVideo) },
                colors = ButtonDefaults.colors(
                    containerColor = Saffron,
                    contentColor = TextWhite,
                    focusedContainerColor = Saffron.copy(alpha = 0.85f),
                    focusedContentColor = TextWhite
                )
            ) {
                Text(
                    text = "\u25B6  Play",
                    style = PramanikTvTheme.typography.labelLarge
                )
            }
        }

        // Dots indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            videos.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == currentIndex) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentIndex) Saffron
                            else TextGray.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}
