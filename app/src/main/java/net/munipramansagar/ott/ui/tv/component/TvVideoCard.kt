package net.munipramansagar.ott.ui.tv.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.ui.tv.theme.CardSurface
import net.munipramansagar.ott.ui.tv.theme.FocusBorder
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.TextWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvVideoCard(
    video: Video,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Int = 240
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "cardScale"
    )

    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .scale(scale)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused || it.hasFocus },
            border = CardDefaults.border(
                focusedBorder = Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, FocusBorder)
                )
            ),
            colors = CardDefaults.colors(
                containerColor = CardSurface,
                focusedContainerColor = CardSurface
            ),
            shape = CardDefaults.shape(
                shape = PramanikTvTheme.shapes.card
            )
        ) {
            Box {
                // Thumbnail
                AsyncImage(
                    model = video.thumbnailUrlHQ.ifEmpty { video.thumbnailUrl },
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(PramanikTvTheme.shapes.card)
                )

                // Duration badge
                if (video.durationFormatted.isNotEmpty()) {
                    Text(
                        text = video.durationFormatted,
                        style = PramanikTvTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontSize = PramanikTvTheme.typography.bodyMedium.fontSize
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(
                                Color.Black.copy(alpha = 0.8f),
                                PramanikTvTheme.shapes.badge
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Bottom gradient for readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 3f)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                            )
                        )
                )
            }
        }

        // Title
        Text(
            text = video.title,
            style = PramanikTvTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 2.dp, end = 2.dp)
        )

        // Channel + views
        Text(
            text = buildString {
                if (video.channelName.isNotEmpty()) append(video.channelName)
                if (video.viewCountFormatted.isNotEmpty()) {
                    if (isNotEmpty()) append(" \u2022 ")
                    append(video.viewCountFormatted)
                }
            },
            style = PramanikTvTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, start = 2.dp, end = 2.dp)
        )
    }
}

@Composable
fun TvVideoCardShimmer(
    modifier: Modifier = Modifier,
    cardWidth: Int = 240
) {
    Column(
        modifier = modifier.width(cardWidth.dp)
    ) {
        // Thumbnail shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(PramanikTvTheme.shapes.card)
                .background(shimmerBrush())
        )
        // Title shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 8.dp)
                .background(shimmerBrush(), PramanikTvTheme.shapes.badge)
                .padding(vertical = 8.dp)
        )
        // Subtitle shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(top = 4.dp)
                .background(shimmerBrush(), PramanikTvTheme.shapes.badge)
                .padding(vertical = 6.dp)
        )
    }
}

@Composable
fun shimmerBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2A2A3E),
            Color(0xFF3A3A50),
            Color(0xFF2A2A3E)
        )
    )
}
