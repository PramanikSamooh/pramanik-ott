package net.munipramansagar.ott.ui.tv.component

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.data.model.LiveStream
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Red
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvLiveStreamBanner(
    isLive: Boolean,
    activeStreams: List<LiveStream>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = isLive && activeStreams.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Red.copy(alpha = 0.12f),
                            Saffron.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(vertical = 16.dp)
        ) {
            // LIVE NOW header
            Row(
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "tv_live_header")
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "tv_header_dot"
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alpha(dotAlpha)
                        .clip(CircleShape)
                        .background(Red)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIVE NOW",
                    style = PramanikTvTheme.typography.titleLarge.copy(
                        color = Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stream cards row
            TvLazyRow(
                contentPadding = PaddingValues(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(activeStreams) { stream ->
                    TvLiveStreamCard(
                        stream = stream,
                        onClick = {
                            val intent = Intent(context, PlayerActivity::class.java).apply {
                                putExtra("videoId", stream.videoId)
                                putExtra("videoTitle", stream.title)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvLiveStreamCard(
    stream: LiveStream,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "tv_live_card")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tv_dot_pulse"
    )

    val borderColor = if (isFocused) Red else Red.copy(alpha = 0.4f)
    val bgColor = if (isFocused) Red.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f)

    Box(
        modifier = Modifier
            .width(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 3.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(bgColor)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            // LIVE badge row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(dotAlpha)
                        .clip(CircleShape)
                        .background(Red)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE",
                    style = PramanikTvTheme.typography.labelMedium.copy(
                        color = Red,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stream title
            Text(
                text = stream.title,
                style = PramanikTvTheme.typography.titleMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Channel name
            Text(
                text = stream.channelName,
                style = PramanikTvTheme.typography.bodyMedium.copy(
                    color = TextGray
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Watch Now label
            Text(
                text = if (isFocused) ">> Watch Now <<" else "Watch Now",
                style = PramanikTvTheme.typography.labelLarge.copy(
                    color = if (isFocused) TextWhite else Saffron,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
