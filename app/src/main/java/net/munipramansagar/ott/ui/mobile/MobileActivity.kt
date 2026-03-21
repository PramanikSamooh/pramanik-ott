package net.munipramansagar.ott.ui.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.mobile.component.BottomNavBar
import net.munipramansagar.ott.ui.mobile.navigation.MobileNavGraph
import net.munipramansagar.ott.ui.mobile.navigation.Routes
import net.munipramansagar.ott.ui.mobile.theme.PramanikTheme
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

                // Determine if current screen is a detail/inner screen that needs a back arrow
                val isInnerScreen = currentRoute.startsWith("section/") ||
                        currentRoute.startsWith("playlist/") ||
                        currentRoute == Routes.SETTINGS

                // Title for inner screens
                val topBarTitle = when {
                    currentRoute == Routes.HOME -> if (isHindi) "प्रामाणिक" else "Pramanik"
                    currentRoute == Routes.SEARCH -> if (isHindi) "खोजें" else "Search"
                    currentRoute == Routes.SETTINGS -> if (isHindi) "सेटिंग्स" else "Settings"
                    currentRoute.startsWith("section/") -> ""  // Will be set by screen
                    currentRoute.startsWith("playlist/") -> "" // Will be set by screen
                    else -> ""
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = topBarTitle,
                                    style = if (currentRoute == Routes.HOME)
                                        MaterialTheme.typography.headlineMedium
                                    else
                                        MaterialTheme.typography.headlineSmall,
                                    color = if (currentRoute == Routes.HOME)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onBackground
                                )
                            },
                            navigationIcon = {
                                if (isInnerScreen) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onBackground
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
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        BottomNavBar(
                            currentRoute = currentRoute,
                            isHindi = isHindi,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME) { saveState = true }
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
