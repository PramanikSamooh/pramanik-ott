package net.munipramansagar.ott.ui.mobile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.Surface
import net.munipramansagar.ott.ui.mobile.theme.TextGray

enum class BottomNavItem(
    val route: String,
    val label: String,
    val labelHi: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
) {
    HOME("home", "Home", "\u0939\u094B\u092E", Icons.Default.Home, Icons.Outlined.Home),
    DISCOURSES("section/pravachan", "Discourses", "\u092A\u094D\u0930\u0935\u091A\u0928", Icons.Default.MenuBook, Icons.Outlined.MenuBook),
    QA("section/shanka-clips", "Q&A", "\u0936\u0902\u0915\u093E", Icons.Default.QuestionAnswer, Icons.Outlined.QuestionAnswer),
    KIDS("section/kids", "Kids", "\u092C\u091A\u094D\u091A\u0947", Icons.Default.Star, Icons.Outlined.StarOutline),
    SEARCH("search", "Search", "\u0916\u094B\u091C\u0947\u0902", Icons.Default.Search, Icons.Outlined.Search);

    fun getLabel(isHindi: Boolean): String = if (isHindi) labelHi else label
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    isHindi: Boolean,
    onNavigate: (String) -> Unit
) {
    Column {
        // Top border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(CardBorder)
        )

        // Nav bar with glass background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface.copy(alpha = 0.92f))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                val isSelected = currentRoute.startsWith(item.route)
                NavBarItem(
                    item = item,
                    isSelected = isSelected,
                    isHindi = isHindi,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    isHindi: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
            contentDescription = item.label,
            tint = if (isSelected) Saffron else TextGray,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.getLabel(isHindi),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Saffron else TextGray,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Saffron dot indicator for selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Saffron)
            )
        } else {
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}
