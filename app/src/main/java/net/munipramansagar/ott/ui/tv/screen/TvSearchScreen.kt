package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.component.TvVideoCard
import net.munipramansagar.ott.ui.tv.theme.DarkBackground
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.Surface
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.viewmodel.SearchViewModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSearchScreen(
    searchViewModel: SearchViewModel
) {
    val uiState by searchViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val onVideoClick: (Video) -> Unit = { video ->
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("videoId", video.id)
            putExtra("videoTitle", video.title)
        }
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 32.dp)
    ) {
        // Search header
        Text(
            text = "Search",
            style = PramanikTvTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .background(Surface, PramanikTvTheme.shapes.button)
                .padding(16.dp)
        ) {
            BasicTextField(
                value = uiState.query,
                onValueChange = { searchViewModel.onQueryChanged(it) },
                textStyle = PramanikTvTheme.typography.titleMedium.copy(color = TextWhite),
                cursorBrush = SolidColor(Saffron),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.query.isEmpty()) {
                            Text(
                                text = "Type to search videos...",
                                style = PramanikTvTheme.typography.titleMedium.copy(
                                    color = TextGray
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Results
        when {
            uiState.isSearching -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Searching...",
                        style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                    )
                }
            }
            uiState.hasSearched && uiState.results.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found",
                        style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                    )
                }
            }
            uiState.results.isNotEmpty() -> {
                TvLazyVerticalGrid(
                    columns = TvGridCells.Adaptive(260.dp),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.results) { video ->
                        TvVideoCard(
                            video = video,
                            onClick = { onVideoClick(video) },
                            cardWidth = 260
                        )
                    }
                }
            }
            else -> {
                // Initial state - no query yet
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search for discourses, swadhyay and more",
                        style = PramanikTvTheme.typography.headlineMedium.copy(color = TextGray)
                    )
                }
            }
        }
    }
}
