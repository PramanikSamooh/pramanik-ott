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
import net.munipramansagar.ott.ui.mobile.screen.CuratedVideosScreen
import net.munipramansagar.ott.ui.mobile.screen.DonateScreen
import net.munipramansagar.ott.ui.mobile.screen.MaharajScreen
import net.munipramansagar.ott.ui.mobile.screen.PoojanScreen
import net.munipramansagar.ott.ui.mobile.screen.ShortsScreen
import net.munipramansagar.ott.ui.mobile.screen.WatchHistoryScreen

object Routes {
    const val HOME = "home"
    const val SHORTS = "shorts"
    const val PATHSHALA = "pathshala"
    const val MAHARAJ = "maharaj"
    const val POOJAN = "poojan"
    const val DONATE = "donate"
    const val CURATED = "curated/{collectionId}/{title}"
    const val SECTION = "section/{sectionId}"

    fun curated(collection: String, title: String) = "curated/$collection/$title"
    const val PLAYLIST = "playlist/{playlistId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val WATCH_HISTORY = "watch_history"

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

        composable(
            route = Routes.CURATED,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            CuratedVideosScreen(
                collection = backStackEntry.arguments?.getString("collectionId") ?: "",
                title = backStackEntry.arguments?.getString("title") ?: "",
                isHindi = isHindi
            )
        }

        composable(Routes.POOJAN) {
            PoojanScreen(
                isHindi = isHindi,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MAHARAJ) {
            MaharajScreen(
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
                onBack = { navController.popBackStack() },
                onNavigateToWatchHistory = { navController.navigate(Routes.WATCH_HISTORY) }
            )
        }

        composable(Routes.WATCH_HISTORY) {
            WatchHistoryScreen(
                onBack = { navController.popBackStack() },
                onVideoClick = { onPlayVideo(it) }
            )
        }
    }
}
