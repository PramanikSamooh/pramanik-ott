package net.munipramansagar.ott.ui.tv.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.data.model.Section
import net.munipramansagar.ott.ui.tv.theme.DarkBg
import net.munipramansagar.ott.ui.tv.theme.DarkBg2
import net.munipramansagar.ott.ui.tv.theme.Red
import net.munipramansagar.ott.ui.tv.theme.GlassBorder
import net.munipramansagar.ott.ui.tv.theme.GlassSurface
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.SaffronDim
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.util.LanguageManager
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.PathshalaViewModel
import net.munipramansagar.ott.viewmodel.SearchViewModel

// Navigation items with grouping support
sealed class TvNavItem(
    val titleEn: String,
    val titleHi: String,
    val icon: ImageVector,
    val isSubItem: Boolean = false
) {
    data object Home : TvNavItem("Home", "होम", Icons.Default.Home)

    // Group headers
    data object MuniGroup : TvNavItem("Muni Pramansagar Ji", "मुनि प्रमाणसागर जी", Icons.Default.PlayCircle)
    data object PathshalaGroup : TvNavItem("Jain Pathshala", "जैन पाठशाला", Icons.Default.School)
    data object PoojanGroup : TvNavItem("Poojan & Path", "पूजन और पाठ", Icons.Default.Favorite)
    data object EventsGroup : TvNavItem("Events", "कार्यक्रम", Icons.Default.Star)
    data object KalyanGroup : TvNavItem("Swa Par Kalyan", "स्व पर कल्याण", Icons.Default.Favorite)

    // Sub-items under Muni Pramansagar Ji
    data object BhawnaYog : TvNavItem("Bhawna Yog", "भावना योग", Icons.Default.PlayCircle, true)
    data object Pravachan : TvNavItem("Pravachan", "प्रवचन", Icons.Default.ViewList, true)
    data object ShankaSamadhan : TvNavItem("Shanka Samadhan", "शंका समाधान", Icons.Default.ViewList, true)
    data object Swadhyay : TvNavItem("Swadhyay", "स्वाध्याय", Icons.Default.ViewList, true)

    // Sub-items under Pathshala
    data object AnimatedVideos : TvNavItem("Animated Videos", "एनिमेटेड वीडियो", Icons.Default.PlayCircle, true)
    data object LiveClasses : TvNavItem("Live Classes", "लाइव कक्षाएँ", Icons.Default.School, true)

    // Sub-items under Poojan & Path
    data object NityaPoojan : TvNavItem("Nitya Poojan", "नित्य पूजन", Icons.Default.Favorite, true)
    data object Path : TvNavItem("Path", "पाठ", Icons.Default.ViewList, true)
    data object Stotra : TvNavItem("Stotra", "स्तोत्र", Icons.Default.ViewList, true)
    data object Bhajan : TvNavItem("Bhajan", "भजन", Icons.Default.PlayCircle, true)
    data object GranthVachan : TvNavItem("Granth Vachan", "ग्रंथ वाचन", Icons.Default.ViewList, true)

    // Sub-items under Events
    data object Programs : TvNavItem("Programs", "कार्यक्रम", Icons.Default.Star, true)

    // Sub-items under Kalyan
    data object Donate : TvNavItem("Donate", "दान", Icons.Default.Favorite, true)

    // Legacy support for dynamic sections
    data class SectionItem(
        val section: Section
    ) : TvNavItem(section.label, section.labelHi, Icons.Default.ViewList, isSubItem = true)

    data object Search : TvNavItem("Search", "खोजें", Icons.Default.Search)
    data object Settings : TvNavItem("Settings", "सेटिंग्स", Icons.Default.Settings)

    fun getTitle(isHindi: Boolean): String = if (isHindi && titleHi.isNotBlank()) titleHi else titleEn
}

// A menu entry can be a group header or a selectable item
data class SidebarEntry(
    val item: TvNavItem,
    val isGroupHeader: Boolean = false,
    val navIndex: Int = -1 // index into navItems for selection
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvApp(
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    pathshalaViewModel: PathshalaViewModel,
    languageManager: LanguageManager
) {
    val language by languageManager.language.collectAsState()
    val isHindi = language == LanguageManager.HINDI
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var isSidebarExpanded by remember { mutableStateOf(false) }

    // Back button: collapse sidebar first, then if on home do nothing (let system handle)
    // If not on home, go to home
    BackHandler(enabled = true) {
        if (isSidebarExpanded) {
            isSidebarExpanded = false
        } else if (selectedIndex != 0) {
            selectedIndex = 0
        }
        // If already on home and sidebar collapsed, system handles back (exit app)
    }

    val uiState by homeViewModel.uiState.collectAsState()

    // Build flat navigation items list (for content switching)
    val navItems = remember(uiState.sections) {
        listOf(
            TvNavItem.Home,          // 0
            TvNavItem.BhawnaYog,     // 1
            TvNavItem.Pravachan,     // 2
            TvNavItem.ShankaSamadhan,// 3
            TvNavItem.Swadhyay,     // 4
            TvNavItem.AnimatedVideos,// 5
            TvNavItem.LiveClasses,  // 6
            TvNavItem.NityaPoojan,  // 7
            TvNavItem.Path,         // 8
            TvNavItem.Stotra,       // 9
            TvNavItem.Bhajan,       // 10
            TvNavItem.GranthVachan, // 11
            TvNavItem.Programs,     // 12
            TvNavItem.Donate,       // 13
            TvNavItem.Search,       // 14
            TvNavItem.Settings      // 15
        )
    }

    // Build sidebar entries with group headers
    val sidebarEntries = remember(navItems) {
        buildList {
            add(SidebarEntry(TvNavItem.Home, navIndex = 0))

            // Muni Pramansagar Ji group
            add(SidebarEntry(TvNavItem.MuniGroup, isGroupHeader = true))
            add(SidebarEntry(TvNavItem.BhawnaYog, navIndex = 1))
            add(SidebarEntry(TvNavItem.Pravachan, navIndex = 2))
            add(SidebarEntry(TvNavItem.ShankaSamadhan, navIndex = 3))
            add(SidebarEntry(TvNavItem.Swadhyay, navIndex = 4))

            // Jain Pathshala group
            add(SidebarEntry(TvNavItem.PathshalaGroup, isGroupHeader = true))
            add(SidebarEntry(TvNavItem.AnimatedVideos, navIndex = 5))
            add(SidebarEntry(TvNavItem.LiveClasses, navIndex = 6))

            // Poojan & Path group
            add(SidebarEntry(TvNavItem.PoojanGroup, isGroupHeader = true))
            add(SidebarEntry(TvNavItem.NityaPoojan, navIndex = 7))
            add(SidebarEntry(TvNavItem.Path, navIndex = 8))
            add(SidebarEntry(TvNavItem.Stotra, navIndex = 9))
            add(SidebarEntry(TvNavItem.Bhajan, navIndex = 10))
            add(SidebarEntry(TvNavItem.GranthVachan, navIndex = 11))

            // Events group
            add(SidebarEntry(TvNavItem.EventsGroup, isGroupHeader = true))
            add(SidebarEntry(TvNavItem.Programs, navIndex = 12))

            // Swa Par Kalyan group
            add(SidebarEntry(TvNavItem.KalyanGroup, isGroupHeader = true))
            add(SidebarEntry(TvNavItem.Donate, navIndex = 13))

            // Bottom
            add(SidebarEntry(TvNavItem.Search, navIndex = 14))
            add(SidebarEntry(TvNavItem.Settings, navIndex = 15))
        }
    }

    // Clamp selectedIndex if sections changed
    val safeIndex = selectedIndex.coerceIn(0, (navItems.size - 1).coerceAtLeast(0))
    if (safeIndex != selectedIndex) {
        selectedIndex = safeIndex
    }

    PramanikTvTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBg, DarkBg2)
                    )
                )
        ) {
            // Content area — full width, sidebar overlays on top
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 56.dp) // Leave space for collapsed sidebar icons
            ) {
                    when (navItems[selectedIndex]) {
                        TvNavItem.Home -> TvHomeScreen(
                            homeViewModel = homeViewModel,
                            isHindi = isHindi,
                            onSectionClick = { sectionId ->
                                // Map section IDs to nav indices
                                val idx = when (sectionId) {
                                    "bhawna-yog" -> navItems.indexOf(TvNavItem.BhawnaYog)
                                    "pravachan" -> navItems.indexOf(TvNavItem.Pravachan)
                                    "shanka-clips", "shanka-full" -> navItems.indexOf(TvNavItem.ShankaSamadhan)
                                    "swadhyay" -> navItems.indexOf(TvNavItem.Swadhyay)
                                    "kids" -> navItems.indexOf(TvNavItem.AnimatedVideos)
                                    "events" -> navItems.indexOf(TvNavItem.Programs)
                                    else -> -1
                                }
                                if (idx >= 0) selectedIndex = idx
                            },
                            pathshalaViewModel = pathshalaViewModel,
                            onPathshalaClick = {
                                selectedIndex = navItems.indexOf(TvNavItem.LiveClasses)
                            }
                        )
                        // Muni Pramansagar Ji sections
                        TvNavItem.BhawnaYog -> TvCategoryScreen(
                            sectionId = "bhawna-yog",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvNavItem.Pravachan -> TvCategoryScreen(
                            sectionId = "pravachan",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvNavItem.ShankaSamadhan -> TvCategoryScreen(
                            sectionId = "shanka-clips",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvNavItem.Swadhyay -> TvCategoryScreen(
                            sectionId = "swadhyay",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        // Pathshala
                        TvNavItem.AnimatedVideos -> TvCategoryScreen(
                            sectionId = "kids",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvNavItem.LiveClasses -> TvPathshalaScreen(
                            pathshalaViewModel = pathshalaViewModel,
                            isHindi = isHindi
                        )
                        // Poojan & Path (placeholder — will show relevant playlists)
                        TvNavItem.NityaPoojan -> net.munipramansagar.ott.ui.mobile.screen.CuratedVideosScreen(
                            collection = "curated_nitya_poojan",
                            title = if (isHindi) "नित्य पूजन" else "Nitya Poojan",
                            isHindi = isHindi
                        )
                        TvNavItem.Path -> net.munipramansagar.ott.ui.mobile.screen.CuratedVideosScreen(
                            collection = "curated_path",
                            title = if (isHindi) "पाठ" else "Path",
                            isHindi = isHindi
                        )
                        TvNavItem.Stotra -> net.munipramansagar.ott.ui.mobile.screen.CuratedVideosScreen(
                            collection = "curated_stotra",
                            title = if (isHindi) "स्तोत्र" else "Stotra",
                            isHindi = isHindi
                        )
                        TvNavItem.Bhajan -> net.munipramansagar.ott.ui.mobile.screen.CuratedVideosScreen(
                            collection = "curated_bhajan",
                            title = if (isHindi) "भजन" else "Bhajan",
                            isHindi = isHindi
                        )
                        TvNavItem.GranthVachan -> net.munipramansagar.ott.ui.mobile.screen.CuratedVideosScreen(
                            collection = "curated_granth_vachan",
                            title = if (isHindi) "ग्रंथ वाचन" else "Granth Vachan",
                            isHindi = isHindi
                        )
                        // Events
                        TvNavItem.Programs -> TvCategoryScreen(
                            sectionId = "events",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        // Donate
                        TvNavItem.Donate -> net.munipramansagar.ott.ui.mobile.screen.DonateScreen(
                            isHindi = isHindi
                        )
                        // Search & Settings
                        TvNavItem.Search -> TvSearchScreen(
                            searchViewModel = searchViewModel
                        )
                        TvNavItem.Settings -> TvSettingsScreen(
                            isHindi = isHindi,
                            onLanguageChange = { lang -> languageManager.setLanguage(lang) }
                        )
                        // Group headers + legacy — fallback to home
                        else -> TvHomeScreen(
                            homeViewModel = homeViewModel,
                            isHindi = isHindi,
                            onSectionClick = {},
                            onPathshalaClick = {}
                        )
                    }
                }

            // Sidebar overlays on top of content
            TvSidebar(
                entries = sidebarEntries,
                selectedIndex = selectedIndex,
                isHindi = isHindi,
                isLive = uiState.liveStatus.isLive,
                isExpanded = isSidebarExpanded,
                onExpandChanged = { isSidebarExpanded = it },
                onItemSelected = { index ->
                    selectedIndex = index
                    isSidebarExpanded = false // collapse after selection
                }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvSidebar(
    entries: List<SidebarEntry>,
    selectedIndex: Int,
    isHindi: Boolean,
    isLive: Boolean,
    isExpanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    onItemSelected: (Int) -> Unit
) {
    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) 260.dp else 56.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "sidebarWidth"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f),
        label = "textAlpha"
    )
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 0.92f else 0.7f,
        animationSpec = tween(200),
        label = "overlayAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(sidebarWidth)
            .background(Color(0xFF0A0A0A).copy(alpha = overlayAlpha))
            .selectableGroup()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        // App logo — only when expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "प्रमाणिक",
                    style = PramanikTvTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.alpha(textAlpha)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Render entries with group headers and sub-items
        entries.forEach { entry ->
            if (entry.isGroupHeader) {
                if (isExpanded) {
                    // Show group header label when expanded
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(0.5.dp)
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = entry.item.getTitle(isHindi).uppercase(),
                        style = PramanikTvTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .alpha(textAlpha)
                    )
                } else {
                    // Show group icon when collapsed
                    TvSidebarItem(
                        item = entry.item,
                        isSelected = false,
                        isExpanded = false,
                        textAlpha = 0f,
                        isHindi = isHindi,
                        onFocusChange = { focused ->
                            if (focused) onExpandChanged(true)
                        },
                        onContentFocusLost = {},
                        onClick = { onExpandChanged(true) }
                    )
                }
            } else if (entry.item.isSubItem) {
                // Sub-items only visible when expanded
                if (isExpanded) {
                    TvSidebarItem(
                        item = entry.item,
                        isSelected = selectedIndex == entry.navIndex,
                        isExpanded = true,
                        textAlpha = textAlpha,
                        isHindi = isHindi,
                        isSubItem = true,
                        onFocusChange = { focused ->
                            if (focused) onExpandChanged(true)
                        },
                        onContentFocusLost = { onExpandChanged(false) },
                        onClick = { if (entry.navIndex >= 0) onItemSelected(entry.navIndex) }
                    )
                }
            } else {
                // Top-level items — always visible
                TvSidebarItem(
                    item = entry.item,
                    isSelected = selectedIndex == entry.navIndex,
                    isExpanded = isExpanded,
                    textAlpha = textAlpha,
                    isHindi = isHindi,
                    showLiveDot = isLive && entry.item is TvNavItem.Home,
                    onFocusChange = { focused ->
                        if (focused) onExpandChanged(true)
                    },
                    onContentFocusLost = { onExpandChanged(false) },
                    onClick = { if (entry.navIndex >= 0) onItemSelected(entry.navIndex) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvSidebarItem(
    item: TvNavItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    textAlpha: Float,
    isHindi: Boolean,
    isSubItem: Boolean = false,
    showLiveDot: Boolean = false,
    onFocusChange: (Boolean) -> Unit,
    onContentFocusLost: () -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val bgColor = when {
        isFocused -> Color.White.copy(alpha = 0.12f)
        isSelected -> Color.White.copy(alpha = 0.05f)
        else -> Color.Transparent
    }
    val contentColor = when {
        isFocused -> Color.White
        isSelected -> Color.White
        else -> Color.White.copy(alpha = 0.5f)
    }
    val accentWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "accentWidth"
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .padding(start = if (isSubItem && isExpanded) 12.dp else 0.dp)
            .fillMaxWidth()
            .height(if (isSubItem) 40.dp else 44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .onFocusChanged {
                isFocused = it.isFocused
                onFocusChange(it.isFocused)
                if (!it.hasFocus && !it.isFocused) {
                    onContentFocusLost()
                }
            }
            .focusable()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active accent bar on the left
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(accentWidth)
                    .height(24.dp)
                    .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                    .background(Color.White)
            )
        }

        // Icon with optional live dot
        Box(
            modifier = Modifier
                .width(if (isExpanded) 48.dp else 56.dp)
                .padding(start = if (isSelected) 8.dp else 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.titleEn,
                tint = contentColor,
                modifier = Modifier.size(if (isSubItem) 18.dp else 22.dp)
            )
            if (showLiveDot) {
                val liveDotTransition = rememberInfiniteTransition(label = "sidebar_live")
                val liveDotAlpha by liveDotTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sidebar_live_dot"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(liveDotAlpha)
                        .clip(CircleShape)
                        .background(Red)
                        .align(Alignment.TopEnd)
                )
            }
        }

        // Label (only when expanded)
        if (isExpanded) {
            Text(
                text = item.getTitle(isHindi),
                style = PramanikTvTheme.typography.labelLarge.copy(
                    color = contentColor,
                    fontSize = if (isSubItem) 13.sp else 14.sp
                ),
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}
