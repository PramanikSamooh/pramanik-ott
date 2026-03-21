package net.munipramansagar.ott.ui.mobile.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import net.munipramansagar.ott.ui.mobile.theme.Background
import net.munipramansagar.ott.ui.mobile.theme.OnSurfaceVariant
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.Surface

enum class BottomNavItem(
    val route: String,
    val label: String,
    val labelHi: String,
    val icon: ImageVector
) {
    HOME("home", "Home", "होम", Icons.Default.Home),
    DISCOURSES("section/discourse", "Discourses", "प्रवचन", Icons.Default.MenuBook),
    QA("section/shanka-clips", "Q&A", "शंका", Icons.Default.QuestionAnswer),
    KIDS("section/kids", "Kids", "बच्चे", Icons.Default.Star),
    SEARCH("search", "Search", "खोजें", Icons.Default.Search);

    fun getLabel(isHindi: Boolean): String = if (isHindi) labelHi else label
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    isHindi: Boolean,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        contentColor = OnSurfaceVariant
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute.startsWith(item.route),
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.getLabel(isHindi),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Saffron,
                    selectedTextColor = Saffron,
                    unselectedIconColor = OnSurfaceVariant,
                    unselectedTextColor = OnSurfaceVariant,
                    indicatorColor = Background
                )
            )
        }
    }
}
