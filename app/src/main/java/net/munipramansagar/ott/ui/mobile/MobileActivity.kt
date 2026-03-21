package net.munipramansagar.ott.ui.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.mobile.component.BottomNavBar
import net.munipramansagar.ott.ui.mobile.navigation.MobileNavGraph
import net.munipramansagar.ott.ui.mobile.navigation.Routes
import net.munipramansagar.ott.ui.mobile.theme.Background
import net.munipramansagar.ott.ui.mobile.theme.PramanikTheme
import net.munipramansagar.ott.ui.mobile.theme.Saffron
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

        setContent {
            PramanikTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME
                val language by languageManager.language.collectAsState()
                val isHindi = language == LanguageManager.HINDI

                // Determine if current screen is a detail/inner screen
                val isInnerScreen = currentRoute.startsWith("section/") ||
                        currentRoute.startsWith("playlist/") ||
                        currentRoute == Routes.SETTINGS ||
                        currentRoute == Routes.PATHSHALA
                val isShortsScreen = currentRoute == Routes.SHORTS

                // Title for inner screens
                val topBarTitle = when {
                    currentRoute == Routes.HOME -> if (isHindi) "\u092A\u094D\u0930\u093E\u092E\u093E\u0923\u093F\u0915" else "Pramanik"
                    currentRoute == Routes.SHORTS -> if (isHindi) "\u0936\u0949\u0930\u094D\u091F\u094D\u0938" else "Shorts"
                    currentRoute == Routes.SEARCH -> if (isHindi) "\u0916\u094B\u091C\u0947\u0902" else "Search"
                    currentRoute == Routes.PATHSHALA -> if (isHindi) "\u092A\u093E\u0920\u0936\u093E\u0932\u093E" else "Pathshala"
                    currentRoute == Routes.SETTINGS -> if (isHindi) "\u0938\u0947\u091F\u093F\u0902\u0917\u094D\u0938" else "Settings"
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
                                    style = if (currentRoute == Routes.HOME)
                                        MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    else
                                        MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                    color = if (currentRoute == Routes.HOME) Saffron else TextWhite
                                )
                            },
                            navigationIcon = {
                                if (isInnerScreen) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = TextWhite
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentRoute == Routes.HOME) {
                                    IconButton(onClick = {
                                        navController.navigate(Routes.SETTINGS)
                                    }) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = TextGray,
                                            modifier = Modifier.size(22.dp)
                                        )
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
                        BottomNavBar(
                            currentRoute = currentRoute,
                            isHindi = isHindi,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    // Pop everything back to home, then navigate
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
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
