package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.component.TvHeroBanner
import net.munipramansagar.ott.ui.tv.component.TvLiveStreamBanner
import net.munipramansagar.ott.ui.tv.component.TvVideoCard
import net.munipramansagar.ott.ui.tv.component.TvVideoCardShimmer
import net.munipramansagar.ott.ui.tv.component.shimmerBrush
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.SaffronLight
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.PathshalaViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen(
    homeViewModel: HomeViewModel,
    isHindi: Boolean,
    onSectionClick: (String) -> Unit,
    pathshalaViewModel: PathshalaViewModel? = null,
    onPathshalaClick: () -> Unit = {}
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val continueWatching by homeViewModel.continueWatching.collectAsState(initial = emptyList())
    val pathshalaState = pathshalaViewModel?.uiState?.collectAsState()
    val context = LocalContext.current

    val onVideoClick: (Video) -> Unit = { video ->
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("videoId", video.id)
            putExtra("videoTitle", video.title)
        }
        context.startActivity(intent)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            TvHomeShimmer()
        } else if (uiState.error != null) {
            TvErrorState(
                message = uiState.error ?: "Something went wrong",
                onRetry = { homeViewModel.refresh() }
            )
        } else {
            // Fixed layout — no scrolling, everything fits on one screen
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Live stream banner — topmost when live
                if (uiState.liveStatus.isLive && uiState.liveStatus.activeStreams.isNotEmpty()) {
                    TvLiveStreamBanner(
                        isLive = uiState.liveStatus.isLive,
                        activeStreams = uiState.liveStatus.activeStreams
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 2. Hero carousel — pinned/latest videos, D-pad navigable
                if (uiState.heroBannerVideos.isNotEmpty()) {
                    TvHeroBanner(
                        videos = uiState.heroBannerVideos,
                        onPlayClick = onVideoClick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 3. Pathshala Today (30%) + Notifications (70%) side by side
                val todaysPathshalaClasses = pathshalaState?.value?.todaysClasses
                val tvAnnouncements = uiState.announcements.filter { it.showOnTv }
                val hasPathshala = todaysPathshalaClasses != null && todaysPathshalaClasses.isNotEmpty()
                val hasAnnouncements = tvAnnouncements.isNotEmpty()

                if (hasPathshala || hasAnnouncements) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (hasPathshala) {
                            Box(modifier = Modifier.weight(if (hasAnnouncements) 0.3f else 1f)) {
                                TvPathshalaTodayCard(
                                    todaysClasses = todaysPathshalaClasses!!,
                                    isHindi = isHindi,
                                    onViewPathshala = onPathshalaClick
                                )
                            }
                        }
                        if (hasAnnouncements) {
                            Box(modifier = Modifier.weight(if (hasPathshala) 0.7f else 1f)) {
                                TvAnnouncementRow(
                                    announcements = tvAnnouncements,
                                    isHindi = isHindi
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 4. Continue Watching — max 3 items + "More" button
                if (continueWatching.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)) {
                        Text(
                            text = if (isHindi) "जारी रखें" else "Continue Watching",
                            style = PramanikTvTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold, fontSize = 18.sp
                            ),
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    TvLazyRow(
                        contentPadding = PaddingValues(horizontal = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Show max 3 items
                        val displayItems = continueWatching.take(3)
                        items(displayItems.size) { index ->
                            val entry = displayItems[index]
                            val video = Video(
                                id = entry.videoId,
                                title = entry.title,
                                titleHi = entry.titleHi,
                                thumbnailUrl = entry.thumbnailUrl.ifEmpty { "https://i.ytimg.com/vi/${entry.videoId}/mqdefault.jpg" },
                                thumbnailUrlHQ = "https://i.ytimg.com/vi/${entry.videoId}/hqdefault.jpg",
                                channelName = entry.channelName,
                                durationFormatted = entry.durationFormatted
                            )
                            TvVideoCard(
                                video = video,
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
                        // "More" button if more than 3
                        if (continueWatching.size > 3) {
                            item {
                                var moreFocused by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(100.dp)
                                        .background(
                                            if (moreFocused) Saffron.copy(alpha = 0.2f)
                                            else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (moreFocused) Saffron else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .onFocusChanged { moreFocused = it.isFocused }
                                        .focusable()
                                        .clickable { /* navigate to watch history */ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isHindi) "और देखें ›" else "More ›",
                                        style = PramanikTvTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (moreFocused) SaffronLight else TextGray
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
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvHomeShimmer() {
    TvLazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        // Banner shimmer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(shimmerBrush())
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Row shimmers
        items(3) {
            Column(modifier = Modifier.padding(horizontal = 48.dp)) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(20.dp)
                        .background(shimmerBrush(), RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    repeat(5) {
                        TvVideoCardShimmer()
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
            )
            Spacer(modifier = Modifier.height(20.dp))
            androidx.tv.material3.Button(
                onClick = onRetry,
                colors = androidx.tv.material3.ButtonDefaults.colors(
                    containerColor = Saffron,
                    contentColor = TextWhite,
                    focusedContainerColor = SaffronLight,
                    focusedContentColor = TextWhite
                ),
                shape = androidx.tv.material3.ButtonDefaults.shape(
                    shape = PramanikTvTheme.shapes.button
                )
            ) {
                Text(
                    "Retry",
                    style = PramanikTvTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvAnnouncementRow(
    announcements: List<net.munipramansagar.ott.data.model.Announcement>,
    isHindi: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
    ) {
        Text(
            text = if (isHindi) "सूचनाएँ" else "Notifications",
            style = PramanikTvTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(12.dp))

        TvLazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(announcements.size) { index ->
                val a = announcements[index]
                val bgColor = when (a.type) {
                    "event" -> net.munipramansagar.ott.ui.tv.theme.Saffron.copy(alpha = 0.12f)
                    "quote" -> net.munipramansagar.ott.ui.tv.theme.Gold.copy(alpha = 0.08f)
                    "whatsapp" -> androidx.compose.ui.graphics.Color(0xFF25D366).copy(alpha = 0.1f)
                    else -> net.munipramansagar.ott.ui.tv.theme.GlassCard
                }
                val borderColor = when (a.type) {
                    "event" -> net.munipramansagar.ott.ui.tv.theme.Saffron.copy(alpha = 0.3f)
                    "whatsapp" -> androidx.compose.ui.graphics.Color(0xFF25D366).copy(alpha = 0.3f)
                    else -> net.munipramansagar.ott.ui.tv.theme.GlassBorder
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                var isFocused by androidx.compose.runtime.mutableStateOf(false)
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .background(
                            if (isFocused) bgColor.copy(alpha = bgColor.alpha + 0.1f) else bgColor,
                            RoundedCornerShape(14.dp)
                        )
                        .then(
                            if (isFocused) Modifier.border(2.dp, borderColor, RoundedCornerShape(14.dp))
                            else Modifier.border(1.dp, borderColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        )
                        .onFocusChanged { isFocused = it.isFocused }
                        .focusable()
                        .clickable {
                            if (a.actionUrl.isNotBlank()) {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(a.actionUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = a.getTitle(isHindi),
                            style = PramanikTvTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TextWhite,
                            maxLines = 2
                        )
                        if (a.getBody(isHindi).isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = a.getBody(isHindi),
                                style = PramanikTvTheme.typography.bodyMedium,
                                color = TextGray,
                                maxLines = 2
                            )
                        }
                        if (a.getActionLabel(isHindi).isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = a.getActionLabel(isHindi),
                                style = PramanikTvTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when (a.type) {
                                        "event" -> net.munipramansagar.ott.ui.tv.theme.Saffron
                                        "whatsapp" -> androidx.compose.ui.graphics.Color(0xFF25D366)
                                        else -> net.munipramansagar.ott.ui.tv.theme.SaffronLight
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
