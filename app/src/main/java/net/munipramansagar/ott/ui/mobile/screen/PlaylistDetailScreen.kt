package net.munipramansagar.ott.ui.mobile.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import net.munipramansagar.ott.ui.mobile.component.VideoGrid
import net.munipramansagar.ott.viewmodel.CategoryViewModel

@Composable
fun PlaylistDetailScreen(
    isHindi: Boolean,
    onVideoClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Playlist title shown below the Scaffold top bar
        val playlistTitle = state.selectedPlaylist?.title
        if (playlistTitle != null) {
            Text(
                text = playlistTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onBack) {
                            Text("Go Back", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            else -> {
                VideoGrid(
                    videos = state.playlistVideos,
                    onVideoClick = { onVideoClick(it.id) },
                    onLoadMore = { viewModel.loadMore() },
                    isLoadingMore = state.isLoadingMore
                )
            }
        }
    }
}
