package net.munipramansagar.ott.ui.mobile.screen

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.munipramansagar.ott.data.local.WatchHistoryEntry
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import net.munipramansagar.ott.data.model.Announcement
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.mobile.component.LiveStreamBanner
import net.munipramansagar.ott.ui.mobile.component.VideoCard
import net.munipramansagar.ott.ui.mobile.theme.CardBg
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.Gold
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.SaffronDark
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextMuted
import net.munipramansagar.ott.ui.mobile.theme.TextWhite
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.PathshalaViewModel

// 4 main categories
data class MainCategory(
    val id: String,
    val labelEn: String,
    val labelHi: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

val mainCategories = listOf(
    MainCategory("bhawna", "Bhawna Yog", "भावना योग", Icons.Default.SelfImprovement, Color(0xFF4CAF50), "section/bhawna-yog"),
    MainCategory("pravachan", "Pravachan", "प्रवचन", Icons.Default.MenuBook, Saffron, "section/pravachan"),
    MainCategory("shanka", "Shanka Samadhan", "शंका समाधान", Icons.Default.QuestionAnswer, Gold, "section/shanka-clips"),
    MainCategory("swadhyay", "Swadhyay", "स्वाध्याय", Icons.Default.MenuBook, Color(0xFF9C27B0), "section/swadhyay"),
)

@OptIn(ExperimentalFoundationApi::class)
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
    val continueWatching by viewModel.continueWatching.collectAsState(initial = emptyList())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // ── 1. Live Stream Banner ──
        LiveStreamBanner(
            isLive = state.liveStatus.isLive,
            activeStreams = state.liveStatus.activeStreams,
            onWatchClick = { videoId -> onVideoClick(videoId) }
        )

        // ── 2. Maharaj Shree Card (photo + name) ──
        Spacer(modifier = Modifier.height(8.dp))
        MaharajCard(
            isHindi = isHindi,
            onClick = { onNavigate("maharaj") }
        )

        // ── 3. Four Category Icons (1x4 row) ──
        Spacer(modifier = Modifier.height(12.dp))
        MainCategoryGrid(
            categories = mainCategories,
            isHindi = isHindi,
            onCategoryClick = { cat -> onNavigate(cat.route) }
        )

        // ── 4. Notification Carousel ──
        val mobileAnnouncements = state.announcements.filter { it.showOnMobile }
        if (mobileAnnouncements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            NotificationCarousel(
                announcements = mobileAnnouncements,
                isHindi = isHindi
            )
        }

        // ── 5. Hero Videos (pinned/latest — same as TV hero) ──
        if (state.heroBannerVideos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = if (isHindi) "नवीनतम वीडियो" else "Latest Videos"
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.heroBannerVideos.take(8), key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video.id) }
                    )
                }
            }
        }

        // ── 6. Continue Watching ──
        if (continueWatching.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = if (isHindi) "जारी रखें" else "Continue Watching"
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(continueWatching, key = { it.videoId }) { entry ->
                    ContinueWatchingCard(
                        entry = entry,
                        isHindi = isHindi,
                        onClick = {
                            val intent = Intent(context, PlayerActivity::class.java).apply {
                                putExtra("videoId", entry.videoId)
                                putExtra("videoTitle", entry.title)
                                putExtra("videoTitleHi", entry.titleHi)
                                putExtra("videoThumbnail", entry.thumbnailUrl)
                                putExtra("playlistId", entry.playlistId)
                                putExtra("playlistTitle", entry.playlistTitle)
                                putExtra("sectionId", entry.sectionId)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// ── Notification Carousel (auto-scrolling) ──
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationCarousel(
    announcements: List<Announcement>,
    isHindi: Boolean
) {
    val pagerState = rememberPagerState(pageCount = { announcements.size })

    // Auto-scroll every 4 seconds
    LaunchedEffect(pagerState) {
        while (true) {
            delay(4000)
            val next = (pagerState.currentPage + 1) % announcements.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val a = announcements[page]
            NotificationCard(announcement = a, isHindi = isHindi)
        }

        // Page indicators
        if (announcements.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                announcements.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (i == pagerState.currentPage) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) Saffron
                                else TextMuted.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    announcement: Announcement,
    isHindi: Boolean
) {
    val bgColor = when (announcement.type) {
        "event" -> Saffron.copy(alpha = 0.12f)
        "quote" -> Gold.copy(alpha = 0.08f)
        "whatsapp" -> Color(0xFF25D366).copy(alpha = 0.1f)
        else -> CardBg
    }
    val borderColor = when (announcement.type) {
        "event" -> Saffron.copy(alpha = 0.25f)
        "quote" -> Gold.copy(alpha = 0.2f)
        "whatsapp" -> Color(0xFF25D366).copy(alpha = 0.2f)
        else -> CardBorder
    }
    val actionColor = when (announcement.type) {
        "event" -> Saffron
        "whatsapp" -> Color(0xFF25D366)
        else -> Saffron
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable {
                if (announcement.actionUrl.isNotBlank()) {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(announcement.actionUrl)
                    )
                    context.startActivity(intent)
                }
            }
            .padding(14.dp)
    ) {
        Text(
            text = announcement.getTitle(isHindi),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (announcement.getBody(isHindi).isNotBlank()) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = announcement.getBody(isHindi),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (announcement.getActionLabel(isHindi).isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = announcement.getActionLabel(isHindi),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = actionColor
            )
        }
    }
}

// ── Maharaj Shree Card ──
@Composable
private fun MaharajCard(
    isHindi: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Saffron.copy(alpha = 0.08f), Gold.copy(alpha = 0.04f))
                )
            )
            .border(1.dp, Saffron.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pramanik logo
        Image(
            painter = painterResource(id = net.munipramansagar.ott.R.drawable.pramanik_logo),
            contentDescription = "Pramanik",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = if (isHindi) "मुनि श्री प्रमाणसागर जी महाराज" else "Muni Shri Pramansagar Ji Maharaj",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isHindi) "जीवन परिचय • फोटो • आरती • पूजन" else "Biography • Photos • Aarti • Poojan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── 2x2 Main Category Grid ──
@Composable
private fun MainCategoryGrid(
    categories: List<MainCategory>,
    isHindi: Boolean,
    onCategoryClick: (MainCategory) -> Unit
) {
    val rows = categories.chunked(2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { cat ->
                    CategoryCard(
                        category = cat,
                        isHindi = isHindi,
                        onClick = { onCategoryClick(cat) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: MainCategory,
    isHindi: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        category.color.copy(alpha = 0.15f),
                        category.color.copy(alpha = 0.03f)
                    )
                )
            )
            .border(1.dp, category.color.copy(alpha = 0.12f), cardShape)
            .clickable { onClick() }
            .padding(vertical = 18.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with circle background
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.labelEn,
                tint = category.color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isHindi) category.labelHi else category.labelEn,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}

// ── Section Header ──
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// ── Continue Watching Card ──
@Composable
private fun ContinueWatchingCard(
    entry: WatchHistoryEntry,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    val progress = if (entry.totalDurationMs > 0)
        (entry.resumePositionMs.toFloat() / entry.totalDurationMs).coerceIn(0f, 1f)
    else 0f

    Column(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // Thumbnail with progress bar
        Box {
            AsyncImage(
                model = entry.thumbnailUrl.ifEmpty { "https://i.ytimg.com/vi/${entry.videoId}/mqdefault.jpg" },
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            // Progress bar at bottom of thumbnail
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(3.dp)
                            .background(Saffron)
                    )
                }
            }
            // Play icon overlay
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        // Title
        Text(
            text = if (isHindi && entry.titleHi.isNotBlank()) entry.titleHi else entry.title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
