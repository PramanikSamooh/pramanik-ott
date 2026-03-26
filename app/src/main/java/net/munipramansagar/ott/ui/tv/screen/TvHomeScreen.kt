package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateFloat
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
    val bookmarkedVideos by homeViewModel.bookmarkedVideos.collectAsState(initial = emptyList())
    val pathshalaState = pathshalaViewModel?.uiState?.collectAsState()
    val context = LocalContext.current
    val lazyListState = androidx.tv.foundation.lazy.list.rememberTvLazyListState()

    val onVideoClick: (Video) -> Unit = { video ->
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("videoId", video.id)
            putExtra("videoTitle", video.title)
        }
        context.startActivity(intent)
    }

    // Scroll to top when hero changes or returning from video
    androidx.compose.runtime.LaunchedEffect(uiState.heroBannerVideos.size) {
        lazyListState.scrollToItem(0)
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
            TvLazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp)
            ) {
                // 1. Live stream banner — topmost when live
                if (uiState.liveStatus.isLive && uiState.liveStatus.activeStreams.isNotEmpty()) {
                    item {
                        TvLiveStreamBanner(
                            isLive = uiState.liveStatus.isLive,
                            activeStreams = uiState.liveStatus.activeStreams
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // 1b. Upcoming live streams — show when not live but scheduled
                if (!uiState.liveStatus.isLive && uiState.liveStatus.upcomingVideos.isNotEmpty()) {
                    item {
                        TvUpcomingBanner(
                            upcomingStreams = uiState.liveStatus.upcomingVideos,
                            isHindi = isHindi
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // 2. Hero carousel — pinned/latest videos, D-pad navigable
                if (uiState.heroBannerVideos.isNotEmpty()) {
                    item {
                        TvHeroBanner(
                            videos = uiState.heroBannerVideos,
                            onPlayClick = onVideoClick
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // 3. Pathshala Today (30%) + Notifications (70%) side by side
                val todaysPathshalaClasses = pathshalaState?.value?.todaysClasses
                val tvAnnouncements = uiState.announcements.filter { it.showOnTv }
                val hasPathshala = todaysPathshalaClasses != null && todaysPathshalaClasses.isNotEmpty()
                val hasAnnouncements = tvAnnouncements.isNotEmpty()

                if (hasPathshala || hasAnnouncements) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (hasPathshala) {
                                Column(modifier = Modifier.weight(if (hasAnnouncements) 0.3f else 1f)) {
                                    Text(
                                        text = if (isHindi) "पाठशाला" else "Pathshala",
                                        style = PramanikTvTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TvPathshalaTodayCard(
                                        todaysClasses = todaysPathshalaClasses!!,
                                        isHindi = isHindi,
                                        onViewPathshala = onPathshalaClick
                                    )
                                }
                            }
                            if (hasAnnouncements) {
                                Column(modifier = Modifier.weight(if (hasPathshala) 0.7f else 1f)) {
                                    TvAnnouncementRow(
                                        announcements = tvAnnouncements,
                                        isHindi = isHindi
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Bookmarked Videos
                if (bookmarkedVideos.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isHindi) "सहेजे गए वीडियो ★" else "Saved Videos ★",
                            style = PramanikTvTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold, color = Color(0xFFE8730A)
                            ),
                            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    item {
                        androidx.tv.foundation.lazy.list.TvLazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bookmarkedVideos.take(5)) { entry ->
                                val video = Video(
                                    id = entry.videoId, title = entry.title,
                                    thumbnailUrl = entry.thumbnailUrl.ifEmpty { "https://i.ytimg.com/vi/${entry.videoId}/hqdefault.jpg" },
                                    channelName = entry.channelName
                                )
                                TvVideoCard(video = video, onClick = { onVideoClick(video) })
                            }
                        }
                    }
                }

                // Continue Watching
                if (continueWatching.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isHindi) "जारी रखें" else "Continue Watching",
                            style = PramanikTvTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold, color = TextWhite
                            ),
                            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    item {
                        androidx.tv.foundation.lazy.list.TvLazyRow(
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(continueWatching.take(5)) { entry ->
                                val video = Video(
                                    id = entry.videoId, title = entry.title,
                                    thumbnailUrl = entry.thumbnailUrl.ifEmpty { "https://i.ytimg.com/vi/${entry.videoId}/hqdefault.jpg" },
                                    channelName = entry.channelName
                                )
                                TvVideoCard(video = video, onClick = { onVideoClick(video) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvHomeShimmer() {
    // Shimmer sweep animation — light bar moves left to right
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A)),
        contentAlignment = Alignment.Center
    ) {
        // Aura glow — pulsing saffron radial gradient behind Maharaj Ji
        val auraTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "aura")
        val auraAlpha by auraTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.35f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(2500, easing = androidx.compose.animation.core.EaseInOut),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "auraAlpha"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE8730A).copy(alpha = auraAlpha),
                            Color(0xFFC9932A).copy(alpha = auraAlpha * 0.5f),
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        // Static image
        coil.compose.AsyncImage(
            model = net.munipramansagar.ott.R.drawable.skeleton_loader,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Shimmer sweep — subtle light bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        startX = shimmerOffset * 2000f,
                        endX = (shimmerOffset + 0.5f) * 2000f
                    )
                )
        )
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isHindi) "सूचनाएँ" else "Notifications",
            style = PramanikTvTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(8.dp))

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
                var isFocused by remember { androidx.compose.runtime.mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .background(
                            if (isFocused) bgColor.copy(alpha = bgColor.alpha + 0.15f) else bgColor,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            if (isFocused) 2.dp else 1.dp,
                            if (isFocused) Saffron else borderColor.copy(alpha = 0.2f),
                            RoundedCornerShape(14.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Text content
                        Column(modifier = Modifier.weight(1f)) {
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
                                Spacer(modifier = Modifier.height(6.dp))
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
                        // QR code for actionable links (TV can't open browser)
                        if (a.actionUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            QrCodeImage(url = a.actionUrl)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QrCodeImage(url: String, qrSize: Int = 72) {
    val qrBitmap = remember(url) {
        try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L
            )
            val matrix = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 200, 200, hints)
            val w = matrix.width
            val h = matrix.height
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            for (x in 0 until w) {
                for (y in 0 until h) {
                    bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        } catch (_: Exception) { null }
    }

    if (qrBitmap != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(qrSize.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val imageBitmap = qrBitmap.asImageBitmap()
                    drawImage(
                        image = imageBitmap,
                        dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Scan",
                style = PramanikTvTheme.typography.labelMedium.copy(
                    color = TextGray,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvUpcomingBanner(
    upcomingStreams: List<net.munipramansagar.ott.data.model.UpcomingStream>,
    isHindi: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A237E).copy(alpha = 0.3f),
                        Color(0xFF0D47A1).copy(alpha = 0.15f)
                    )
                )
            )
            .border(1.dp, Color(0xFF42A5F5).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clock icon + "Upcoming" label
        Text(
            text = "🕐",
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isHindi) "आगामी लाइव" else "Upcoming Live",
                style = PramanikTvTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF90CAF9)
                )
            )
            upcomingStreams.take(2).forEach { stream ->
                Text(
                    text = "${stream.title} • ${formatScheduledTime(stream.scheduledStart)}",
                    style = PramanikTvTheme.typography.bodyMedium,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatScheduledTime(isoTime: String): String {
    return try {
        val instant = java.time.Instant.parse(isoTime)
        val zoned = instant.atZone(java.time.ZoneId.of("Asia/Kolkata"))
        val hour = zoned.hour
        val minute = zoned.minute
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        "$displayHour:${minute.toString().padStart(2, '0')} $amPm IST"
    } catch (_: Exception) {
        isoTime
    }
}
