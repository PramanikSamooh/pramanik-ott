package net.munipramansagar.ott.ui.tv.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.ui.tv.theme.DarkBackground
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.Surface
import net.munipramansagar.ott.ui.tv.theme.TextGray
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            // Sidebar
            TvSidebar(
                selectedIndex = selectedIndex,
                isHindi = isHindi,
                onItemSelected = { index -> selectedIndex = index }
            )

            // Content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(DarkBackground)
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvSidebar(
    selectedIndex: Int,
    isHindi: Boolean,
    onItemSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(220.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Surface, Surface.copy(alpha = 0.95f))
                )
            )
            .padding(vertical = 24.dp)
            .selectableGroup()
    ) {
        // App logo/name
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "\u092A\u094D\u0930\u093E\u092E\u093E\u0923\u093F\u0915",
                style = PramanikTvTheme.typography.headlineLarge.copy(
                    color = Saffron
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation items
        navItems.forEachIndexed { index, screen ->
            val isSelected = selectedIndex == index
            var isFocused by remember { mutableStateOf(false) }

            val bgColor = when {
                isFocused -> Saffron.copy(alpha = 0.25f)
                isSelected -> Saffron.copy(alpha = 0.15f)
                else -> Color.Transparent
            }
            val contentColor = when {
                isFocused || isSelected -> Saffron
                else -> TextGray
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(bgColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .onFocusChanged { isFocused = it.isFocused }
                    .focusable()
                    .clickable { onItemSelected(index) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.titleEn,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isHindi) screen.titleHi else screen.titleEn,
                    style = PramanikTvTheme.typography.labelLarge.copy(color = contentColor)
                )
            }
        }
    }
}
