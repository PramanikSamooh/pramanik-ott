package net.munipramansagar.ott.ui.mobile.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import net.munipramansagar.ott.ui.mobile.component.LiveStreamBanner
import net.munipramansagar.ott.ui.mobile.component.ShimmerVideoRow
import net.munipramansagar.ott.ui.mobile.component.VideoCard
import net.munipramansagar.ott.ui.mobile.theme.Background
import net.munipramansagar.ott.ui.mobile.theme.CardBg
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.Gold
import net.munipramansagar.ott.ui.mobile.theme.KidsBlue
import net.munipramansagar.ott.ui.mobile.theme.LiveRed
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.SaffronDark
import net.munipramansagar.ott.ui.mobile.theme.SaffronLight
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextMuted
import net.munipramansagar.ott.ui.mobile.theme.TextWhite
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.PathshalaViewModel

// Hub menu items
data class HubMenuItem(
    val id: String,
    val labelEn: String,
    val labelHi: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
) {
    fun getLabel(isHindi: Boolean) = if (isHindi) labelHi else labelEn
}

val hubMenuItems = listOf(
    HubMenuItem("pravachan", "Pravachan", "प्रवचन", Icons.Default.MenuBook, Saffron, "section/pravachan"),
    HubMenuItem("shanka", "Shanka\nSamadhan", "शंका\nसमाधान", Icons.Default.QuestionAnswer, Gold, "section/shanka-clips"),
    HubMenuItem("bhawna", "Bhawna\nYog", "भावना\nयोग", Icons.Default.SelfImprovement, Color(0xFF4CAF50), "section/bhawna-yog"),
    HubMenuItem("swadhyay", "Swadhyay", "स्वाध्याय", Icons.Default.Spa, Color(0xFF9C27B0), "section/swadhyay"),
    HubMenuItem("pathshala", "Pathshala", "पाठशाला", Icons.Default.School, KidsBlue, "pathshala"),
    HubMenuItem("shorts", "Shorts", "शॉर्ट्स", Icons.Default.PlayCircle, Color(0xFFFF5722), "shorts"),
    HubMenuItem("live", "Live", "लाइव", Icons.Default.LiveTv, LiveRed, "live"),
    HubMenuItem("search", "Search", "खोजें", Icons.Default.Search, Color(0xFF607D8B), "search"),
    HubMenuItem("settings", "Settings", "सेटिंग्स", Icons.Default.Settings, TextGray, "settings"),
)

@Composable
fun HomeScreen(
    isHindi: Boolean,
    onVideoClick: (String) -> Unit,
    onViewAllClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit = {},
    onPathshalaClick: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    pathshalaViewModel: PathshalaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Live Stream Banner — top priority
        LiveStreamBanner(
            isLive = state.liveStatus.isLive,
            activeStreams = state.liveStatus.activeStreams,
            onWatchClick = { videoId -> onVideoClick(videoId) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Hub Menu Grid
        HubMenuGrid(
            items = hubMenuItems,
            isHindi = isHindi,
            isLive = state.liveStatus.isLive,
            onItemClick = { item ->
                when (item.id) {
                    "pathshala" -> onPathshalaClick()
                    "live" -> {
                        if (state.liveStatus.isLive && state.liveStatus.activeStreams.isNotEmpty()) {
                            onVideoClick(state.liveStatus.activeStreams.first().videoId)
                        }
                    }
                    else -> onNavigate(item.route)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Videos section
        if (state.heroBannerVideos.isNotEmpty()) {
            Text(
                text = if (isHindi) "हाल के वीडियो" else "Recent Videos",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = TextWhite,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.heroBannerVideos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video.id) }
                    )
                }
            }
        }

        // First section preview (if available)
        if (state.sections.isNotEmpty()) {
            val firstSection = state.sections.first()
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = firstSection.section.getLabel(isHindi),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = TextWhite
                )
                Text(
                    text = if (isHindi) "सभी देखें >" else "View All >",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Saffron,
                    modifier = Modifier.clickable {
                        onViewAllClick(firstSection.section.id)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            firstSection.playlists.firstOrNull()?.let { playlistWithVideos ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlistWithVideos.videos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            onClick = { onVideoClick(video.id) }
                        )
                    }
                }
            }
        }

        // Bottom padding
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun HubMenuGrid(
    items: List<HubMenuItem>,
    isHindi: Boolean,
    isLive: Boolean,
    onItemClick: (HubMenuItem) -> Unit
) {
    // 3 columns grid
    val rows = items.chunked(3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    HubMenuCircle(
                        item = item,
                        isHindi = isHindi,
                        isLive = isLive && item.id == "live",
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if row has < 3 items
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HubMenuCircle(
    item: HubMenuItem,
    isHindi: Boolean,
    isLive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val livePulse = if (isLive) {
        val transition = rememberInfiniteTransition(label = "live_pulse")
        val alpha by transition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "live_alpha"
        )
        alpha
    } else 1f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circle icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            item.color.copy(alpha = 0.25f),
                            item.color.copy(alpha = 0.08f)
                        )
                    )
                )
                .border(
                    1.5.dp,
                    item.color.copy(alpha = if (isLive) livePulse * 0.8f else 0.4f),
                    CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.labelEn,
                tint = item.color,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label
        Text(
            text = item.getLabel(isHindi),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            ),
            color = TextWhite.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
