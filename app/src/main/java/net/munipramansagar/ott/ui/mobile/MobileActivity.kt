package net.munipramansagar.ott.ui.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        if (currentRoute == Routes.HOME) {
                            TopAppBar(
                                title = {
                                    androidx.compose.material3.Text(
                                        text = if (isHindi) "प्रामाणिक" else "Pramanik",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                actions = {
                                    IconButton(onClick = {
                                        navController.navigate(Routes.SETTINGS)
                                    }) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        }
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
