package net.munipramansagar.ott.ui.tv.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.ui.tv.theme.DarkBg
import net.munipramansagar.ott.ui.tv.theme.GlassBorder
import net.munipramansagar.ott.ui.tv.theme.GlassCard
import net.munipramansagar.ott.ui.tv.theme.GlassHighlight
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvVideoCard(
    video: Video,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Int = 256
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "cardScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f),
        label = "cardAlpha"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 400f),
        label = "borderWidth"
    )

    val cardShape = PramanikTvTheme.shapes.card

    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .scale(scale)
            .alpha(contentAlpha)
    ) {
        // Saffron accent line at top when focused
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isFocused) 2.dp else 0.dp)
                .background(if (isFocused) Saffron else Color.Transparent)
        )

        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
                .then(
                    if (isFocused) {
                        Modifier.border(
                            BorderStroke(borderWidth, Saffron),
                            cardShape
                        )
                    } else {
                        Modifier.border(
                            BorderStroke(1.dp, GlassBorder),
                            cardShape
                        )
                    }
                ),
            colors = CardDefaults.colors(
                containerColor = GlassCard,
                focusedContainerColor = GlassHighlight
            ),
            shape = CardDefaults.shape(shape = cardShape)
        ) {
            Column {
                // Thumbnail area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PramanikTvTheme.shapes.cardTop)
                ) {
                    AsyncImage(
                        model = video.thumbnailUrlHQ.ifEmpty { video.thumbnailUrl },
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )

                    // Bottom gradient overlay on thumbnail (30%)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 3f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        DarkBg.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    // Duration pill – glass style
                    if (video.durationFormatted.isNotEmpty()) {
                        Text(
                            text = video.durationFormatted,
                            style = PramanikTvTheme.typography.labelMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.55f),
                                    PramanikTvTheme.shapes.badge
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Text content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // Title
                    Text(
                        text = video.title,
                        style = PramanikTvTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Channel + views
                    val subtitle = buildString {
                        if (video.channelName.isNotEmpty()) append(video.channelName)
                        if (video.viewCountFormatted.isNotEmpty()) {
                            if (isNotEmpty()) append(" \u2022 ")
                            append(video.viewCountFormatted)
                        }
                    }
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = PramanikTvTheme.typography.labelMedium.copy(
                                color = TextGray,
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TvVideoCardShimmer(
    modifier: Modifier = Modifier,
    cardWidth: Int = 256
) {
    val cardShape = PramanikTvTheme.shapes.card

    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .border(BorderStroke(1.dp, GlassBorder), cardShape)
            .clip(cardShape)
            .background(GlassCard)
    ) {
        // Thumbnail shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(shimmerBrush())
        )
        Column(modifier = Modifier.padding(12.dp)) {
            // Title shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .background(shimmerBrush(), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(10.dp)
                    .background(shimmerBrush(), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun shimmerBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF161628),
            Color(0xFF222240),
            Color(0xFF161628)
        )
    )
}
