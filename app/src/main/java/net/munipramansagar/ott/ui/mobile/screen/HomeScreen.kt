package net.munipramansagar.ott.ui.mobile.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.munipramansagar.ott.ui.mobile.component.HeroBanner
import net.munipramansagar.ott.ui.mobile.component.ShimmerVideoRow
import net.munipramansagar.ott.ui.mobile.component.VideoRow
import net.munipramansagar.ott.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    isHindi: Boolean,
    onVideoClick: (String) -> Unit,
    onViewAllClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                repeat(4) { ShimmerVideoRow() }
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { viewModel.refresh() }) {
                        Text("Retry", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Banner
                if (state.heroBannerVideos.isNotEmpty()) {
                    HeroBanner(
                        videos = state.heroBannerVideos,
                        onVideoClick = { onVideoClick(it.id) }
                    )
                }

                // Category rows
                state.rows.forEach { row ->
                    VideoRow(
                        title = if (isHindi) row.labelHi else row.label,
                        videos = row.videos,
                        onVideoClick = { onVideoClick(it.id) },
                        onViewAllClick = { onViewAllClick(row.categorySlug) }
                    )
                }

                // Bottom padding
                Box(modifier = Modifier.padding(bottom = 80.dp))
            }
        }
    }
}
