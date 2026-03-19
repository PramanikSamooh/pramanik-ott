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
import net.munipramansagar.ott.ui.mobile.screen.SearchScreen
import net.munipramansagar.ott.ui.mobile.screen.SettingsScreen

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category/{categorySlug}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    fun category(slug: String) = "category/$slug"
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
                onViewAllClick = { navController.navigate(Routes.category(it)) }
            )
        }

        composable(
            route = Routes.CATEGORY,
            arguments = listOf(navArgument("categorySlug") { type = NavType.StringType })
        ) {
            CategoryScreen(
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

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
