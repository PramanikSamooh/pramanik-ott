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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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

sealed class TvScreen(
    val titleEn: String,
    val titleHi: String,
    val icon: ImageVector
) {
    data object Home : TvScreen("Home", "\u0939\u094B\u092E", Icons.Default.Home)
    data object Discourses : TvScreen("Discourses", "\u092A\u094D\u0930\u0935\u091A\u0928", Icons.Default.Book)
    data object BhawnaYog : TvScreen("Bhawna Yog", "\u092D\u093E\u0935\u0928\u093E \u092F\u094B\u0917", Icons.Default.Favorite)
    data object QnA : TvScreen("Q&A", "\u0936\u0902\u0915\u093E \u0938\u092E\u093E\u0927\u093E\u0928", Icons.Default.QuestionAnswer)
    data object Kids : TvScreen("Pathshala", "\u092A\u093E\u0920\u0936\u093E\u0932\u093E", Icons.Default.ChildCare)
    data object Search : TvScreen("Search", "\u0916\u094B\u091C\u0947\u0902", Icons.Default.Search)
    data object Settings : TvScreen("Settings", "\u0938\u0947\u091F\u093F\u0902\u0917\u094D\u0938", Icons.Default.Settings)
}

private val navItems = listOf(
    TvScreen.Home,
    TvScreen.Discourses,
    TvScreen.BhawnaYog,
    TvScreen.QnA,
    TvScreen.Kids,
    TvScreen.Search,
    TvScreen.Settings
)

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

    PramanikTvTheme {
        // Full-screen gradient background
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
                    when (navItems[selectedIndex]) {
                        TvScreen.Home -> TvHomeScreen(
                            homeViewModel = homeViewModel,
                            isHindi = isHindi,
                            onCategoryClick = { slug ->
                                val idx = navItems.indexOfFirst {
                                    when (it) {
                                        TvScreen.Discourses -> slug == "discourse"
                                        TvScreen.BhawnaYog -> slug == "bhawna-yog"
                                        TvScreen.QnA -> slug == "shanka-clips" || slug == "shanka-full"
                                        TvScreen.Kids -> slug == "kids"
                                        else -> false
                                    }
                                }
                                if (idx >= 0) selectedIndex = idx
                            }
                        )
                        TvScreen.Discourses -> TvCategoryScreen(
                            categorySlug = "discourse",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvScreen.BhawnaYog -> TvCategoryScreen(
                            categorySlug = "bhawna-yog",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvScreen.QnA -> TvCategoryScreen(
                            categorySlug = "shanka-clips",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvScreen.Kids -> TvCategoryScreen(
                            categorySlug = "kids",
                            homeViewModel = homeViewModel,
                            isHindi = isHindi
                        )
                        TvScreen.Search -> TvSearchScreen(
                            searchViewModel = searchViewModel
                        )
                        TvScreen.Settings -> TvSettingsScreen(
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
                    text = "\u092A\u094D\u0930\u093E\u092E\u093E\u0923\u093F\u0915",
                    style = PramanikTvTheme.typography.headlineLarge.copy(
                        color = Saffron,
                        fontSize = 24.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                // Collapsed: show "P" initial in saffron
                Text(
                    text = "\u092A\u094D\u0930",
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

        // Navigation items (excluding Settings which goes to bottom)
        val mainItems = navItems.dropLast(1)
        val settingsItem = navItems.last()

        mainItems.forEachIndexed { index, screen ->
            TvSidebarItem(
                screen = screen,
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
            screen = settingsItem,
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
    screen: TvScreen,
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
                imageVector = screen.icon,
                contentDescription = screen.titleEn,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // Label (only when expanded)
        if (isExpanded) {
            Text(
                text = if (isHindi) screen.titleHi else screen.titleEn,
                style = PramanikTvTheme.typography.labelLarge.copy(
                    color = contentColor,
                    fontSize = 14.sp
                ),
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}
