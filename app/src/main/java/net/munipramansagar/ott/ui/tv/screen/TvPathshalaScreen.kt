package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import net.munipramansagar.ott.data.model.PathshalaClass
import net.munipramansagar.ott.data.model.Teacher
import net.munipramansagar.ott.ui.tv.theme.GlassCard
import net.munipramansagar.ott.ui.tv.theme.GlassBorder
import net.munipramansagar.ott.ui.tv.theme.GlassHighlight
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.SaffronDim
import net.munipramansagar.ott.ui.tv.theme.SaffronLight
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextMuted
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.viewmodel.PathshalaViewModel

private val HindiColor = Saffron
private val EnglishColor = Color(0xFF2196F3)
private val MarathiColor = Color(0xFF4CAF50)
private val GujaratiColor = Color(0xFF9C27B0)

private val DayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
private val DayNamesHi = listOf("\u0930\u0935\u093F\u0935\u093E\u0930", "\u0938\u094B\u092E\u0935\u093E\u0930", "\u092E\u0902\u0917\u0932\u0935\u093E\u0930", "\u092C\u0941\u0927\u0935\u093E\u0930", "\u0917\u0941\u0930\u0941\u0935\u093E\u0930", "\u0936\u0941\u0915\u094D\u0930\u0935\u093E\u0930", "\u0936\u0928\u093F\u0935\u093E\u0930")

private val LanguageFilters = listOf(null, "hindi", "english", "marathi", "gujarati")
private val LanguageLabels = listOf("All", "Hindi", "English", "Marathi", "Gujarati")

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPathshalaScreen(
    pathshalaViewModel: PathshalaViewModel,
    isHindi: Boolean
) {
    val state by pathshalaViewModel.uiState.collectAsState()
    val context = LocalContext.current

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "\u0932\u094B\u0921 \u0939\u094B \u0930\u0939\u093E \u0939\u0948..." else "Loading...",
                    style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                )
            }
        }

        state.error != null && state.allClasses.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error ?: "",
                        style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    var retryFocused by remember { mutableStateOf(false) }
                    androidx.tv.material3.Button(
                        onClick = { pathshalaViewModel.refresh() },
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
                        Text("Retry", style = PramanikTvTheme.typography.labelLarge)
                    }
                }
            }
        }

        else -> {
            TvLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp)
            ) {
                // Language filter row
                item {
                    TvLanguageFilterRow(
                        selectedLanguage = state.selectedLanguage,
                        isHindi = isHindi,
                        onLanguageSelected = { pathshalaViewModel.setLanguageFilter(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val todaysClasses = pathshalaViewModel.getFilteredTodaysClasses()
                val classesByDay = pathshalaViewModel.getFilteredClassesByDay()

                // Today's Classes
                if (todaysClasses.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isHindi) "\u0906\u091C \u0915\u0940 \u0915\u0915\u094D\u0937\u093E\u090F\u0901" else "Today's Classes",
                            style = PramanikTvTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Saffron,
                                fontSize = 22.sp
                            ),
                            modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        TvLazyRow(
                            contentPadding = PaddingValues(horizontal = 48.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(todaysClasses) { cls ->
                                TvPathshalaClassCard(
                                    pathshalaClass = cls,
                                    teacher = state.teachers[cls.teacherId],
                                    isHindi = isHindi,
                                    isLive = pathshalaViewModel.isClassLive(cls),
                                    isUpcoming = pathshalaViewModel.isClassUpcoming(cls),
                                    onJoinClick = {
                                        if (cls.youtubeLink.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cls.youtubeLink))
                                            context.startActivity(intent)
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Weekly Schedule header
                item {
                    Text(
                        text = if (isHindi) "\u0938\u093E\u092A\u094D\u0924\u093E\u0939\u093F\u0915 \u0938\u092E\u092F-\u0938\u093E\u0930\u0923\u0940" else "Weekly Schedule",
                        style = PramanikTvTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp)
                    )
                }

                if (classesByDay.isEmpty()) {
                    item { TvEmptyState(isHindi = isHindi) }
                } else {
                    val orderedDays = listOf(1, 2, 3, 4, 5, 6, 0)
                    orderedDays.forEach { day ->
                        val dayClasses = classesByDay[day]
                        if (dayClasses != null && dayClasses.isNotEmpty()) {
                            item {
                                TvDayHeader(
                                    day = day,
                                    isToday = day == state.currentDayOfWeek,
                                    isHindi = isHindi
                                )
                            }
                            item {
                                TvLazyRow(
                                    contentPadding = PaddingValues(horizontal = 48.dp),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    items(dayClasses) { cls ->
                                        TvPathshalaClassCard(
                                            pathshalaClass = cls,
                                            teacher = state.teachers[cls.teacherId],
                                            isHindi = isHindi,
                                            isLive = pathshalaViewModel.isClassLive(cls),
                                            isUpcoming = pathshalaViewModel.isClassUpcoming(cls),
                                            onJoinClick = {
                                                if (cls.youtubeLink.isNotBlank()) {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cls.youtubeLink))
                                                    context.startActivity(intent)
                                                }
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
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
private fun TvLanguageFilterRow(
    selectedLanguage: String?,
    isHindi: Boolean,
    onLanguageSelected: (String?) -> Unit
) {
    TvLazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(LanguageFilters.size) { index ->
            val language = LanguageFilters[index]
            val isSelected = selectedLanguage == language
            val label = LanguageLabels[index]
            val chipColor = when (language) {
                "hindi" -> HindiColor
                "english" -> EnglishColor
                "marathi" -> MarathiColor
                "gujarati" -> GujaratiColor
                else -> Saffron
            }

            var isFocused by remember { mutableStateOf(false) }

            Text(
                text = label,
                style = PramanikTvTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = when {
                    isSelected -> Color.White
                    isFocused -> TextWhite
                    else -> TextGray
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when {
                            isSelected -> chipColor.copy(alpha = 0.85f)
                            isFocused -> GlassHighlight
                            else -> GlassCard
                        }
                    )
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = when {
                            isFocused -> Saffron
                            isSelected -> chipColor
                            else -> GlassBorder
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .focusable()
                    .clickable { onLanguageSelected(language) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvDayHeader(
    day: Int,
    isToday: Boolean,
    isHindi: Boolean
) {
    val dayName = if (isHindi) DayNamesHi[day] else DayNames[day]
    val displayText = if (isToday) {
        "$dayName ${if (isHindi) "(\u0906\u091C)" else "(Today)"}"
    } else {
        dayName
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Saffron)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = displayText,
            style = PramanikTvTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            color = if (isToday) Saffron else TextWhite
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvPathshalaClassCard(
    pathshalaClass: PathshalaClass,
    teacher: Teacher?,
    isHindi: Boolean,
    isLive: Boolean,
    isUpcoming: Boolean,
    onJoinClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(12.dp)

    val infiniteTransition = rememberInfiniteTransition(label = "tv_class")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = if (isLive) 0.5f else 1f,
        targetValue = if (isLive) 1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tv_border_pulse"
    )

    val borderColor = when {
        isFocused -> Saffron
        isLive -> Saffron.copy(alpha = borderAlpha)
        else -> GlassBorder
    }

    val languageBadgeColor = when (pathshalaClass.language.lowercase()) {
        "hindi" -> HindiColor
        "english" -> EnglishColor
        "marathi" -> MarathiColor
        "gujarati" -> GujaratiColor
        else -> TextGray
    }

    Column(
        modifier = Modifier
            .width(320.dp)
            .clip(cardShape)
            .background(if (isFocused) GlassHighlight else GlassCard)
            .border(
                BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor),
                cardShape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onJoinClick() }
            .padding(16.dp)
    ) {
        // Title + Language badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pathshalaClass.getTitle(isHindi),
                style = PramanikTvTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = pathshalaClass.language.replaceFirstChar { it.uppercase() },
                style = PramanikTvTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(languageBadgeColor.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Teacher
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (teacher?.photoUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = teacher.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(GlassBorder),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = teacher?.getName(isHindi) ?: pathshalaClass.teacherName,
                style = PramanikTvTheme.typography.bodyMedium,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time + status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime12Hour(pathshalaClass.time),
                style = PramanikTvTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextGray
            )

            if (isLive) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(net.munipramansagar.ott.ui.tv.theme.Red.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val liveDotTransition = rememberInfiniteTransition(label = "tv_live_dot")
                    val dotAlpha by liveDotTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "tv_live_dot_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = dotAlpha))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE",
                        style = PramanikTvTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }
            } else if (isUpcoming) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Saffron.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "\u091C\u0941\u0921\u093C\u0947\u0902" else "Join",
                        style = PramanikTvTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvEmptyState(isHindi: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isHindi) "\u0915\u094B\u0908 \u0915\u0915\u094D\u0937\u093E \u0928\u093F\u0930\u094D\u0927\u093E\u0930\u093F\u0924 \u0928\u0939\u0940\u0902 \u0939\u0948" else "No classes scheduled",
                style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
            )
        }
    }
}

/** Pathshala Today card for TvHomeScreen */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPathshalaTodayCard(
    todaysClasses: List<PathshalaClass>,
    isHindi: Boolean,
    onViewPathshala: () -> Unit
) {
    if (todaysClasses.isEmpty()) return

    var isFocused by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 8.dp)
            .clip(cardShape)
            .background(if (isFocused) SaffronDim else GlassCard)
            .border(
                BorderStroke(
                    if (isFocused) 2.dp else 1.dp,
                    if (isFocused) Saffron else Saffron.copy(alpha = 0.3f)
                ),
                cardShape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onViewPathshala() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.School,
            contentDescription = null,
            tint = Saffron,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isHindi) "\u092A\u093E\u0920\u0936\u093E\u0932\u093E \u0906\u091C" else "Pathshala Today",
                style = PramanikTvTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Saffron
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            todaysClasses.take(3).forEach { cls ->
                Text(
                    text = "\u2022 ${cls.getTitle(isHindi)} - ${formatTime12Hour(cls.time)}",
                    style = PramanikTvTheme.typography.bodyMedium,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (todaysClasses.size > 3) {
                Text(
                    text = if (isHindi) "...और ${todaysClasses.size - 3} कक्षाएँ" else "...and ${todaysClasses.size - 3} more",
                    style = PramanikTvTheme.typography.bodyMedium.copy(color = TextGray)
                )
            }
        }
        Text(
            text = "${todaysClasses.size} ${if (isHindi) "\u0915\u0915\u094D\u0937\u093E\u090F\u0901" else "classes"}",
            style = PramanikTvTheme.typography.labelLarge.copy(color = TextGray)
        )
    }
}

private fun formatTime12Hour(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time
    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1]
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:$minute $amPm"
}
