package net.munipramansagar.ott.ui.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.munipramansagar.ott.ui.mobile.screen.CategoryScreen
import net.munipramansagar.ott.ui.mobile.screen.HomeScreen
import net.munipramansagar.ott.ui.mobile.screen.PathshalaScreen
import net.munipramansagar.ott.ui.mobile.screen.PlaylistDetailScreen
import net.munipramansagar.ott.ui.mobile.screen.SearchScreen
import net.munipramansagar.ott.ui.mobile.screen.SettingsScreen
import net.munipramansagar.ott.ui.mobile.screen.DonateScreen
import net.munipramansagar.ott.ui.mobile.screen.ShortsScreen

object Routes {
    const val HOME = "home"
    const val SHORTS = "shorts"
    const val PATHSHALA = "pathshala"
    const val DONATE = "donate"
    const val SECTION = "section/{sectionId}"
    const val PLAYLIST = "playlist/{playlistId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    fun section(sectionId: String) = "section/$sectionId"
    fun playlist(playlistId: String) = "playlist/$playlistId"
}

@Composable
fun MobileNavGraph(
    navController: NavHostController,
    isHindi: Boolean,
    onPlayVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                isHindi = isHindi,
                onVideoClick = { onPlayVideo(it) },
                onViewAllClick = { navController.navigate(Routes.section(it)) },
                onPlaylistClick = { navController.navigate(Routes.playlist(it)) },
                onPathshalaClick = { navController.navigate(Routes.PATHSHALA) },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Routes.SHORTS) {
            ShortsScreen()
        }

        composable(
            route = Routes.SECTION,
            arguments = listOf(navArgument("sectionId") { type = NavType.StringType })
        ) {
            CategoryScreen(
                isHindi = isHindi,
                onVideoClick = { onPlayVideo(it) },
                onPlaylistClick = { navController.navigate(Routes.playlist(it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PLAYLIST,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) {
            PlaylistDetailScreen(
                isHindi = isHindi,
                onVideoClick = { onPlayVideo(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onVideoClick = { onPlayVideo(it) }
            )
        }

        composable(Routes.PATHSHALA) {
            PathshalaScreen(
                isHindi = isHindi,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DONATE) {
            DonateScreen(
                isHindi = isHindi,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
