package net.munipramansagar.ott.ui.tv.screen

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.items
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.tv.component.TvVideoCard
import net.munipramansagar.ott.ui.tv.theme.GlassBorder
import net.munipramansagar.ott.ui.tv.theme.GlassCard
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextMuted
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
            .padding(top = 32.dp)
    ) {
        // Search header with icon
        Row(
            modifier = Modifier.padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Saffron,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search",
                style = PramanikTvTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search input – glass style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .background(GlassCard, PramanikTvTheme.shapes.searchField)
                .border(
                    BorderStroke(1.dp, GlassBorder),
                    PramanikTvTheme.shapes.searchField
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = uiState.query,
                    onValueChange = { searchViewModel.onQueryChanged(it) },
                    textStyle = PramanikTvTheme.typography.titleMedium.copy(
                        color = TextWhite,
                        fontSize = 17.sp
                    ),
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
                                        color = TextMuted,
                                        fontSize = 17.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recent searches hint
        if (!uiState.hasSearched && uiState.query.isEmpty()) {
            Text(
                text = "Recent searches will appear here",
                style = PramanikTvTheme.typography.labelMedium.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results area
        when {
            uiState.isSearching -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Searching...",
                            style = PramanikTvTheme.typography.headlineMedium.copy(
                                color = TextGray
                            )
                        )
                    }
                }
            }

            uiState.hasSearched && uiState.results.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found",
                            style = PramanikTvTheme.typography.headlineMedium.copy(
                                color = TextGray
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try searching with different keywords",
                            style = PramanikTvTheme.typography.bodyMedium.copy(
                                color = TextMuted
                            )
                        )
                    }
                }
            }

            uiState.results.isNotEmpty() -> {
                TvLazyVerticalGrid(
                    columns = TvGridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.results) { video ->
                        TvVideoCard(
                            video = video,
                            onClick = { onVideoClick(video) },
                            cardWidth = 280
                        )
                    }
                }
            }

            else -> {
                // Initial state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Search for discourses, swadhyay and more",
                            style = PramanikTvTheme.typography.headlineMedium.copy(
                                color = TextGray,
                                fontSize = 20.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
