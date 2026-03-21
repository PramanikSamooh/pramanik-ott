package net.munipramansagar.ott.ui.mobile.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import net.munipramansagar.ott.data.model.PathshalaClass
import net.munipramansagar.ott.data.model.Teacher
import net.munipramansagar.ott.ui.mobile.component.LiveBadge
import net.munipramansagar.ott.ui.mobile.theme.CardBg
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.SaffronDark
import net.munipramansagar.ott.ui.mobile.theme.SaffronLight
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextMuted
import net.munipramansagar.ott.ui.mobile.theme.TextWhite
import net.munipramansagar.ott.viewmodel.PathshalaViewModel

// Language badge colors
private val HindiColor = Saffron
private val EnglishColor = Color(0xFF2196F3)
private val MarathiColor = Color(0xFF4CAF50)
private val GujaratiColor = Color(0xFF9C27B0)

private val DayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
private val DayNamesHi = listOf("\u0930\u0935\u093F\u0935\u093E\u0930", "\u0938\u094B\u092E\u0935\u093E\u0930", "\u092E\u0902\u0917\u0932\u0935\u093E\u0930", "\u092C\u0941\u0927\u0935\u093E\u0930", "\u0917\u0941\u0930\u0941\u0935\u093E\u0930", "\u0936\u0941\u0915\u094D\u0930\u0935\u093E\u0930", "\u0936\u0928\u093F\u0935\u093E\u0930")

private val LanguageFilters = listOf(null, "hindi", "english", "marathi", "gujarati")
private val LanguageLabels = listOf("All", "Hindi", "English", "Marathi", "Gujarati")
private val LanguageLabelsHi = listOf("\u0938\u092D\u0940", "\u0939\u093F\u0928\u094D\u0926\u0940", "\u0905\u0902\u0917\u094D\u0930\u0947\u091C\u0940", "\u092E\u0930\u093E\u0920\u0940", "\u0917\u0941\u091C\u0930\u093E\u0924\u0940")

@Composable
fun PathshalaScreen(
    isHindi: Boolean,
    onBack: () -> Unit,
    viewModel: PathshalaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "\u0932\u094B\u0921 \u0939\u094B \u0930\u0939\u093E \u0939\u0948..." else "Loading...",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyLarge
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
                        color = TextGray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Language filter tabs
                LanguageFilterRow(
                    selectedLanguage = state.selectedLanguage,
                    isHindi = isHindi,
                    onLanguageSelected = { viewModel.setLanguageFilter(it) }
                )

                val todaysClasses = viewModel.getFilteredTodaysClasses()
                val classesByDay = viewModel.getFilteredClassesByDay()

                // Today's Classes section
                if (todaysClasses.isNotEmpty()) {
                    SectionHeader(
                        title = if (isHindi) "\u0906\u091C \u0915\u0940 \u0915\u0915\u094D\u0937\u093E\u090F\u0901" else "Today's Classes",
                        isHighlighted = true
                    )
                    todaysClasses.forEach { pathshalaClass ->
                        val teacher = state.teachers[pathshalaClass.teacherId]
                        PathshalaClassCard(
                            pathshalaClass = pathshalaClass,
                            teacher = teacher,
                            isHindi = isHindi,
                            isLive = viewModel.isClassLive(pathshalaClass),
                            isUpcoming = viewModel.isClassUpcoming(pathshalaClass),
                            onJoinClick = {
                                if (pathshalaClass.youtubeLink.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pathshalaClass.youtubeLink))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Weekly Schedule
                SectionHeader(
                    title = if (isHindi) "\u0938\u093E\u092A\u094D\u0924\u093E\u0939\u093F\u0915 \u0938\u092E\u092F-\u0938\u093E\u0930\u0923\u0940" else "Weekly Schedule"
                )

                if (classesByDay.isEmpty()) {
                    // Empty state
                    EmptyState(isHindi = isHindi)
                } else {
                    // Ordered days starting from Monday (1) through Sunday (0)
                    val orderedDays = listOf(1, 2, 3, 4, 5, 6, 0)
                    orderedDays.forEach { day ->
                        val dayClasses = classesByDay[day]
                        if (dayClasses != null && dayClasses.isNotEmpty()) {
                            DayHeader(
                                day = day,
                                isToday = day == state.currentDayOfWeek,
                                isHindi = isHindi
                            )
                            dayClasses.forEach { pathshalaClass ->
                                val teacher = state.teachers[pathshalaClass.teacherId]
                                PathshalaClassCard(
                                    pathshalaClass = pathshalaClass,
                                    teacher = teacher,
                                    isHindi = isHindi,
                                    isLive = viewModel.isClassLive(pathshalaClass),
                                    isUpcoming = viewModel.isClassUpcoming(pathshalaClass),
                                    onJoinClick = {
                                        if (pathshalaClass.youtubeLink.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pathshalaClass.youtubeLink))
                                            context.startActivity(intent)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Bottom padding for nav bar
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun LanguageFilterRow(
    selectedLanguage: String?,
    isHindi: Boolean,
    onLanguageSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LanguageFilters.forEachIndexed { index, language ->
            val isSelected = selectedLanguage == language
            val label = if (isHindi) LanguageLabelsHi[index] else LanguageLabels[index]
            val chipColor = when (language) {
                "hindi" -> HindiColor
                "english" -> EnglishColor
                "marathi" -> MarathiColor
                "gujarati" -> GujaratiColor
                else -> Saffron
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isSelected) Color.White else TextGray,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) chipColor.copy(alpha = 0.85f)
                        else CardBg
                    )
                    .border(
                        1.dp,
                        if (isSelected) chipColor else CardBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onLanguageSelected(language) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    isHighlighted: Boolean = false
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        color = if (isHighlighted) Saffron else TextWhite,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun DayHeader(
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
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Saffron)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = displayText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isToday) Saffron else TextWhite.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun PathshalaClassCard(
    pathshalaClass: PathshalaClass,
    teacher: Teacher?,
    isHindi: Boolean,
    isLive: Boolean,
    isUpcoming: Boolean,
    onJoinClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(14.dp)
    val borderColor = when {
        isLive -> Saffron
        else -> CardBorder
    }

    val infiniteTransition = rememberInfiniteTransition(label = "class_card")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = if (isLive) 0.5f else 1f,
        targetValue = if (isLive) 1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_pulse"
    )

    val languageBadgeColor = when (pathshalaClass.language.lowercase()) {
        "hindi" -> HindiColor
        "english" -> EnglishColor
        "marathi" -> MarathiColor
        "gujarati" -> GujaratiColor
        else -> TextGray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(cardShape)
            .background(CardBg)
            .border(
                1.dp,
                borderColor.copy(alpha = borderAlpha),
                cardShape
            )
            .padding(14.dp)
    ) {
        // Top row: title + language badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pathshalaClass.getTitle(isHindi),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Language badge
            Text(
                text = pathshalaClass.language.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(languageBadgeColor.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Teacher row
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (teacher?.photoUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = teacher.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CardBorder),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = teacher?.getName(isHindi) ?: pathshalaClass.teacherName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom row: time + live/join button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time display
            Text(
                text = formatTime12Hour(pathshalaClass.time),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextGray
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLive) {
                    LiveBadge()
                }

                if (isLive || isUpcoming) {
                    Button(
                        onClick = onJoinClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLive) Saffron else SaffronDark.copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLive) {
                                if (isHindi) "\u0905\u092D\u0940 \u091C\u0941\u0921\u093C\u0947\u0902" else "Join Now"
                            } else {
                                if (isHindi) "\u091C\u0941\u0921\u093C\u0947\u0902" else "Join"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(isHindi: Boolean) {
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
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isHindi) "\u0915\u094B\u0908 \u0915\u0915\u094D\u0937\u093E \u0928\u093F\u0930\u094D\u0927\u093E\u0930\u093F\u0924 \u0928\u0939\u0940\u0902 \u0939\u0948" else "No classes scheduled",
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray
            )
        }
    }
}

/** Pathshala Today card for the HomeScreen */
@Composable
fun PathshalaTodayCard(
    todaysClasses: List<PathshalaClass>,
    isHindi: Boolean,
    onViewPathshala: () -> Unit
) {
    if (todaysClasses.isEmpty()) return

    val cardShape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Saffron.copy(alpha = 0.12f),
                        SaffronLight.copy(alpha = 0.06f)
                    )
                )
            )
            .border(1.dp, Saffron.copy(alpha = 0.3f), cardShape)
            .clickable { onViewPathshala() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isHindi) "\u092A\u093E\u0920\u0936\u093E\u0932\u093E \u0906\u091C" else "Pathshala Today",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Saffron
                )
            }
            Text(
                text = "${todaysClasses.size} ${if (isHindi) "\u0915\u0915\u094D\u0937\u093E\u090F\u0901" else "classes"}",
                style = MaterialTheme.typography.labelMedium,
                color = TextGray
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Show first 2-3 class titles
        todaysClasses.take(3).forEach { cls ->
            Text(
                text = "\u2022 ${cls.getTitle(isHindi)} - ${formatTime12Hour(cls.time)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
        if (todaysClasses.size > 3) {
            Text(
                text = if (isHindi) "...और ${todaysClasses.size - 3} कक्षाएँ" else "...and ${todaysClasses.size - 3} more",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
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
