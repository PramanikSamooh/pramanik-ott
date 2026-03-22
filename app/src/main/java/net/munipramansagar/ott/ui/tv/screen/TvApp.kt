package net.munipramansagar.ott.ui.tv.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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

// Navigation items: Home, dynamic sections from Firestore, Search, Settings
sealed class TvNavItem(
    val titleEn: String,
    val titleHi: String,
    val icon: ImageVector
) {
    data object Home : TvNavItem("Home", "\u0939\u094B\u092E", Icons.Default.Home)
    data object Shorts : TvNavItem("Shorts", "\u0936\u0949\u0930\u094D\u091F\u094D\u0938", Icons.Default.PlayCircle)
    data object Pathshala : TvNavItem("Pathshala", "\u092A\u093E\u0920\u0936\u093E\u0932\u093E", Icons.Default.School)
    data class SectionItem(
        val section: Section
    ) : TvNavItem(section.label, section.labelHi, Icons.Default.ViewList)
    data object Search : TvNavItem("Search", "खोजें", Icons.Default.Search)
    data object Settings : TvNavItem("Settings", "सेटिंग्स", Icons.Default.Settings)

    fun getTitle(isHindi: Boolean): String = if (isHindi && titleHi.isNotBlank()) titleHi else titleEn
}

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

    val uiState by homeViewModel.uiState.collectAsState()

    // Build navigation items dynamically from loaded sections
    val navItems = remember(uiState.sections) {
        buildList {
            add(TvNavItem.Home)
            add(TvNavItem.Shorts)
            add(TvNavItem.Pathshala)
            uiState.sections.forEach { sectionData ->
                add(TvNavItem.SectionItem(sectionData.section))
            }
            add(TvNavItem.Search)
            add(TvNavItem.Settings)
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
                    when (val item = navItems[selectedIndex]) {
                        TvNavItem.Home -> TvHomeScreen(
                            homeViewModel = homeViewModel,
                            isHindi = isHindi,
                            onSectionClick = { sectionId ->
                                val idx = navItems.indexOfFirst {
                                    it is TvNavItem.SectionItem && it.section.id == sectionId
                                }
                                if (idx >= 0) selectedIndex = idx
                            },
                            pathshalaViewModel = pathshalaViewModel,
                            onPathshalaClick = {
                                val idx = navItems.indexOfFirst { it is TvNavItem.Pathshala }
                                if (idx >= 0) selectedIndex = idx
                            }
                        )
                        TvNavItem.Shorts -> TvShortsScreen(
                            isHindi = isHindi
                        )
                        TvNavItem.Pathshala -> TvPathshalaScreen(
                            pathshalaViewModel = pathshalaViewModel,
                            isHindi = isHindi
                        )
                        is TvNavItem.SectionItem -> TvCategoryScreen(
                            sectionId = item.section.id,
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvNavItem.Search -> TvSearchScreen(
                            searchViewModel = searchViewModel
                        )
                        TvNavItem.Settings -> TvSettingsScreen(
                            isHindi = isHindi,
                            onLanguageChange = { lang -> languageManager.setLanguage(lang) }
                        )
                    }
                }

            // Sidebar overlays on top of content
            TvSidebar(
                navItems = navItems,
                selectedIndex = selectedIndex,
                isHindi = isHindi,
                isLive = uiState.liveStatus.isLive,
                onItemSelected = { index -> selectedIndex = index }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvSidebar(
    navItems: List<TvNavItem>,
    selectedIndex: Int,
    isHindi: Boolean,
    isLive: Boolean,
    onItemSelected: (Int) -> Unit
) {
    var isSidebarFocused by remember { mutableStateOf(false) }

    val sidebarWidth by animateDpAsState(
        targetValue = if (isSidebarFocused) 260.dp else 56.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "sidebarWidth"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isSidebarFocused) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f),
        label = "textAlpha"
    )
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isSidebarFocused) 0.85f else 0.6f,
        animationSpec = tween(200),
        label = "overlayAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(sidebarWidth)
            .background(
                Color(0xFF0D0D0D).copy(alpha = overlayAlpha)
            )
            .padding(vertical = 16.dp)
            .selectableGroup()
            .verticalScroll(rememberScrollState())
    ) {
        // App logo — only when expanded
        if (isSidebarFocused) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "प्रामाणिक",
                    style = PramanikTvTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontSize = 22.sp
                    ),
                    modifier = Modifier.alpha(textAlpha)
                )
            }

            // Separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main navigation items (exclude Settings which goes to bottom)
        val mainItems = navItems.dropLast(1)
        val settingsItem = navItems.last()

        mainItems.forEachIndexed { index, item ->
            TvSidebarItem(
                item = item,
                isSelected = selectedIndex == index,
                isExpanded = isSidebarFocused,
                textAlpha = textAlpha,
                isHindi = isHindi,
                showLiveDot = isLive && item is TvNavItem.Home,
                onFocusChange = { focused ->
                    if (focused) isSidebarFocused = true
                },
                onContentFocusLost = { isSidebarFocused = false },
                onClick = { onItemSelected(index) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Thin separator before settings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.1f))
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Settings at bottom
        TvSidebarItem(
            item = settingsItem,
            isSelected = selectedIndex == navItems.lastIndex,
            isExpanded = isSidebarFocused,
            textAlpha = textAlpha,
            isHindi = isHindi,
            onFocusChange = { focused ->
                if (focused) isSidebarFocused = true
            },
            onContentFocusLost = { isSidebarFocused = false },
            onClick = { onItemSelected(navItems.lastIndex) }
        )
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
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
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
                modifier = Modifier.size(22.dp)
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
                    fontSize = 14.sp
                ),
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}
