package net.munipramansagar.ott.ui.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import net.munipramansagar.ott.ui.tv.screen.TvApp
import net.munipramansagar.ott.util.LanguageManager
import net.munipramansagar.ott.viewmodel.HomeViewModel
import net.munipramansagar.ott.viewmodel.PathshalaViewModel
import net.munipramansagar.ott.viewmodel.SearchViewModel
import javax.inject.Inject

@AndroidEntryPoint
class TvActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val pathshalaViewModel: PathshalaViewModel by viewModels()

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvApp(
                homeViewModel = homeViewModel,
                searchViewModel = searchViewModel,
                pathshalaViewModel = pathshalaViewModel,
                languageManager = languageManager
            )
        }
    }
}
