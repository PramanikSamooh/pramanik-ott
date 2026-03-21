package net.munipramansagar.ott.ui.tv.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import net.munipramansagar.ott.ui.tv.theme.GlassBorder
import net.munipramansagar.ott.ui.tv.theme.GlassSurface
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.SaffronDim
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.util.LanguageManager
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.SearchViewModel

// Navigation items: Home, dynamic sections from Firestore, Search, Settings
sealed class TvNavItem(
    val titleEn: String,
    val titleHi: String,
    val icon: ImageVector
) {
    data object Home : TvNavItem("Home", "होम", Icons.Default.Home)
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
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar
                TvSidebar(
                    navItems = navItems,
                    selectedIndex = selectedIndex,
                    isHindi = isHindi,
                    onItemSelected = { index -> selectedIndex = index }
                )

                // Vertical separator
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(GlassBorder)
                )

                // Content area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
                            }
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
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvSidebar(
    navItems: List<TvNavItem>,
    selectedIndex: Int,
    isHindi: Boolean,
    onItemSelected: (Int) -> Unit
) {
    var isSidebarFocused by remember { mutableStateOf(false) }

    val sidebarWidth by animateDpAsState(
        targetValue = if (isSidebarFocused) 240.dp else 72.dp,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 350f),
        label = "sidebarWidth"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isSidebarFocused) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f),
        label = "textAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(sidebarWidth)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        GlassSurface,
                        Color.Transparent
                    )
                )
            )
            .padding(vertical = 20.dp)
            .selectableGroup()
            .verticalScroll(rememberScrollState())
    ) {
        // App logo / name
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            contentAlignment = if (isSidebarFocused) Alignment.CenterStart else Alignment.Center
        ) {
            if (isSidebarFocused) {
                Text(
                    text = "प्रामाणिक",
                    style = PramanikTvTheme.typography.headlineLarge.copy(
                        color = Saffron,
                        fontSize = 24.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                Text(
                    text = "प्र",
                    style = PramanikTvTheme.typography.headlineLarge.copy(
                        color = Saffron,
                        fontSize = 22.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(GlassBorder)
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                onFocusChange = { focused ->
                    if (focused) isSidebarFocused = true
                },
                onContentFocusLost = { isSidebarFocused = false },
                onClick = { onItemSelected(index) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Separator before settings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(GlassBorder)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
    onFocusChange: (Boolean) -> Unit,
    onContentFocusLost: () -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val bgColor = when {
        isFocused -> SaffronDim
        isSelected -> Color.White.copy(alpha = 0.06f)
        else -> Color.Transparent
    }
    val contentColor = when {
        isFocused -> Saffron
        isSelected -> Saffron
        else -> TextGray
    }
    val accentWidth by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
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
                    .height(28.dp)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(Saffron)
            )
        }

        // Icon
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
