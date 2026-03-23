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
import androidx.compose.foundation.layout.width
import net.munipramansagar.ott.ui.mobile.component.LiveStreamBanner
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
    HubMenuItem("bhawna", "Bhawna Yog", "भावना योग", Icons.Default.SelfImprovement, Color(0xFF4CAF50), "section/bhawna-yog"),
    HubMenuItem("pravachan", "Pravachan", "प्रवचन", Icons.Default.MenuBook, Saffron, "section/pravachan"),
    HubMenuItem("shanka", "Shanka\nSamadhan", "शंका\nसमाधान", Icons.Default.QuestionAnswer, Gold, "section/shanka-clips"),
    HubMenuItem("swadhyay", "Swadhyay", "स्वाध्याय", Icons.Default.Spa, Color(0xFF9C27B0), "section/swadhyay"),
    HubMenuItem("pathshala", "Pathshala", "पाठशाला", Icons.Default.School, KidsBlue, "pathshala"),
    HubMenuItem("poojan", "Poojan\n& Path", "पूजन\nव पाठ", Icons.Default.Spa, Color(0xFFFF9800), "section/poojan"),
    HubMenuItem("events", "Events", "कार्यक्रम", Icons.Default.Stars, Color(0xFFE91E63), "section/events"),
    HubMenuItem("donate", "Swa Par\nKalyan", "स्व पर\nकल्याण", Icons.Default.Stars, Color(0xFF00BCD4), "donate"),
    HubMenuItem("search", "Search", "खोजें", Icons.Default.Search, Color(0xFF607D8B), "search"),
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

        // Bento Grid Hub
        BentoGrid(
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

        // Announcements — events, quotes, WhatsApp
        if (state.announcements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            AnnouncementCards(
                announcements = state.announcements,
                isHindi = isHindi
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
private fun BentoGrid(
    items: List<HubMenuItem>,
    isHindi: Boolean,
    isLive: Boolean,
    onItemClick: (HubMenuItem) -> Unit
) {
    val itemMap = items.associateBy { it.id }
    val gap = 10.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        // Row 1: Hero (Pravachan 2x2) + Shanka (1x2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            itemMap["pravachan"]?.let {
                BentoCard(
                    item = it, isHindi = isHindi, isLive = false,
                    onClick = { onItemClick(it) },
                    modifier = Modifier.weight(2f).aspectRatio(1f)
                )
            }
            itemMap["shanka"]?.let {
                BentoCard(
                    item = it, isHindi = isHindi, isLive = false,
                    onClick = { onItemClick(it) },
                    modifier = Modifier.weight(1f).aspectRatio(0.5f)
                )
            }
        }

        // Row 2: Bhawna + Swadhyay + Pathshala (equal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            listOf("bhawna", "swadhyay", "pathshala").forEach { id ->
                itemMap[id]?.let {
                    BentoCard(
                        item = it, isHindi = isHindi, isLive = false,
                        onClick = { onItemClick(it) },
                        modifier = Modifier.weight(1f).height(90.dp)
                    )
                }
            }
        }

        // Row 3: Shorts + Live + Search (equal small)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            listOf("shorts", "live", "search").forEach { id ->
                itemMap[id]?.let {
                    BentoCard(
                        item = it, isHindi = isHindi,
                        isLive = isLive && id == "live",
                        onClick = { onItemClick(it) },
                        modifier = Modifier.weight(1f).height(76.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BentoCard(
    item: HubMenuItem,
    isHindi: Boolean,
    isLive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val livePulse = if (isLive) {
        val transition = rememberInfiniteTransition(label = "live_pulse")
        val alpha by transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "live_alpha"
        )
        alpha
    } else 1f

    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        item.color.copy(alpha = 0.2f),
                        item.color.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                1.dp,
                item.color.copy(alpha = if (isLive) livePulse * 0.6f else 0.2f),
                cardShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.labelEn,
                tint = item.color.copy(alpha = 0.9f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.getLabel(isHindi),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                ),
                color = TextWhite.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Live pulsing dot
        if (isLive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(LiveRed.copy(alpha = livePulse))
            )
        }
    }
}

@Composable
private fun AnnouncementCards(
    announcements: List<net.munipramansagar.ott.data.model.Announcement>,
    isHindi: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        announcements.forEach { announcement ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val cardShape = RoundedCornerShape(16.dp)
            val bgColor = when (announcement.type) {
                "event" -> Saffron.copy(alpha = 0.12f)
                "quote" -> Gold.copy(alpha = 0.08f)
                "whatsapp" -> Color(0xFF25D366).copy(alpha = 0.1f)
                else -> CardBg
            }
            val borderColor = when (announcement.type) {
                "event" -> Saffron.copy(alpha = 0.3f)
                "quote" -> Gold.copy(alpha = 0.2f)
                "whatsapp" -> Color(0xFF25D366).copy(alpha = 0.3f)
                else -> CardBorder
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(bgColor)
                    .border(1.dp, borderColor, cardShape)
                    .clickable {
                        if (announcement.actionUrl.isNotBlank()) {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(announcement.actionUrl)
                            )
                            context.startActivity(intent)
                        }
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image if available
                if (announcement.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = announcement.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                }

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = announcement.getTitle(isHindi),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextWhite,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (announcement.getBody(isHindi).isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = announcement.getBody(isHindi),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (announcement.getActionLabel(isHindi).isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = announcement.getActionLabel(isHindi),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = when (announcement.type) {
                                "event" -> Saffron
                                "whatsapp" -> Color(0xFF25D366)
                                else -> SaffronLight
                            }
                        )
                    }
                }
            }
        }
    }
}
