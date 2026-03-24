package net.munipramansagar.ott.ui.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.mobile.navigation.MobileNavGraph
import net.munipramansagar.ott.ui.mobile.navigation.Routes
import net.munipramansagar.ott.ui.mobile.theme.Background
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.PramanikTheme
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.Surface
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextWhite
import net.munipramansagar.ott.util.LanguageManager
import javax.inject.Inject

@AndroidEntryPoint
class MobileActivity : ComponentActivity() {

    @Inject
    lateinit var languageManager: LanguageManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for deep link: pramanik://tv-link?code=XXXXXX
        val deepLinkCode = intent?.data?.getQueryParameter("code")

        setContent {
            val themeMode by languageManager.themeMode.collectAsState()
            PramanikTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME
                val language by languageManager.language.collectAsState()
                val isHindi = language == LanguageManager.HINDI

                // Handle deep link — navigate to settings with TV code
                LaunchedEffect(deepLinkCode) {
                    if (!deepLinkCode.isNullOrBlank()) {
                        navController.navigate("${Routes.SETTINGS}?tvCode=$deepLinkCode") {
                            popUpTo(Routes.HOME)
                        }
                    }
                }

                val isHomeScreen = currentRoute == Routes.HOME
                val isInnerScreen = !isHomeScreen && currentRoute != Routes.SHORTS
                val isShortsScreen = currentRoute == Routes.SHORTS

                // Title for inner screens
                val topBarTitle = when {
                    isHomeScreen -> if (isHindi) "प्रमाणिक" else "Pramanik"
                    currentRoute == Routes.SEARCH -> if (isHindi) "खोजें" else "Search"
                    currentRoute == Routes.PATHSHALA -> if (isHindi) "पाठशाला" else "Pathshala"
                    currentRoute == Routes.SETTINGS -> if (isHindi) "सेटिंग्स" else "Settings"
                    currentRoute == Routes.DONATE -> if (isHindi) "स्व पर कल्याण" else "Swa Par Kalyan"
                    currentRoute.startsWith("curated/") -> ""
                    currentRoute.startsWith("section/") -> ""
                    currentRoute.startsWith("playlist/") -> ""
                    else -> ""
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = if (isShortsScreen) Color.Black else Background,
                    topBar = {
                        if (!isShortsScreen) TopAppBar(
                            title = {
                                Text(
                                    text = topBarTitle,
                                    style = if (isHomeScreen)
                                        MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                                    else
                                        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isHomeScreen) Saffron else TextWhite
                                )
                            },
                            navigationIcon = {
                                if (isInnerScreen) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextWhite)
                                    }
                                }
                            },
                            actions = {
                                // Search icon (always on home)
                                if (isHomeScreen) {
                                    IconButton(onClick = {
                                        navController.navigate(Routes.SEARCH)
                                    }) {
                                        Icon(Icons.Default.Search, "Search", tint = TextGray, modifier = Modifier.size(22.dp))
                                    }
                                }
                                // Settings icon (always on home)
                                if (isHomeScreen) {
                                    IconButton(onClick = {
                                        navController.navigate(Routes.SETTINGS)
                                    }) {
                                        Icon(Icons.Default.Settings, "Settings", tint = TextGray, modifier = Modifier.size(22.dp))
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Background.copy(alpha = 0.9f)
                            )
                        )
                    },
                    bottomBar = {
                        // Bottom nav for secondary sections
                        if (!isShortsScreen) {
                            BottomNavigation(
                                currentRoute = currentRoute,
                                isHindi = isHindi,
                                onNavigate = { route ->
                                    if (route == Routes.HOME) {
                                        navController.navigate(Routes.HOME) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate(route) {
                                            popUpTo(Routes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    MobileNavGraph(
                        navController = navController,
                        isHindi = isHindi,
                        onPlayVideo = { videoId ->
                            val intent = Intent(this@MobileActivity, PlayerActivity::class.java)
                            intent.putExtra("videoId", videoId)
                            startActivity(intent)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// ── Bottom Navigation ──
data class BottomNavTab(
    val route: String,
    val labelEn: String,
    val labelHi: String,
    val icon: ImageVector
)

val bottomTabs = listOf(
    BottomNavTab(Routes.HOME, "Home", "होम", Icons.Default.Home),
    BottomNavTab(Routes.PATHSHALA, "Pathshala", "पाठशाला", Icons.Default.School),
    BottomNavTab(net.munipramansagar.ott.ui.mobile.navigation.Routes.POOJAN, "Poojan", "पूजन", Icons.Default.Spa),
    BottomNavTab("section/events", "Events", "कार्यक्रम", Icons.Default.Star),
    BottomNavTab(Routes.DONATE, "Donate", "दान", Icons.Default.Favorite),
)

@androidx.compose.runtime.Composable
private fun BottomNavigation(
    currentRoute: String,
    isHindi: Boolean,
    onNavigate: (String) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(CardBorder)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface.copy(alpha = 0.92f))
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.forEach { tab ->
                val isSelected = when {
                    tab.route == Routes.HOME -> currentRoute == Routes.HOME
                    tab.route == Routes.PATHSHALA -> currentRoute == Routes.PATHSHALA
                    tab.route == Routes.DONATE -> currentRoute == Routes.DONATE
                    else -> currentRoute.startsWith(tab.route.substringBefore("{"))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(tab.route) }
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.labelEn,
                        tint = if (isSelected) Saffron else TextGray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHindi) tab.labelHi else tab.labelEn,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Saffron else TextGray,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
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
        }
    }

}
