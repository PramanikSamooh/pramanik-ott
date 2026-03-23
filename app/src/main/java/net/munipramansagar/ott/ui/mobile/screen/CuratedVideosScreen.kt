package net.munipramansagar.ott.ui.mobile.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.player.PlayerActivity
import net.munipramansagar.ott.ui.mobile.component.VideoCard
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextMuted
import net.munipramansagar.ott.ui.mobile.theme.TextWhite

/**
 * Screen that shows admin-curated videos from a Firestore collection.
 * Each collection (curated_nitya_poojan, curated_path, curated_stotra, curated_granth_vachan)
 * contains documents with: id, title, thumbnailUrl, youtubeVideoId, priority, active
 */
@Composable
fun CuratedVideosScreen(
    collection: String,
    title: String,
    isHindi: Boolean
) {
    val context = LocalContext.current
    var videos by remember { mutableStateOf<List<CuratedVideo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(collection) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection(collection)
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .await()
            videos = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CuratedVideo::class.java)?.copy(id = doc.id)
            }.filter { it.active }
        } catch (_: Exception) {}
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Header
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = TextWhite,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Saffron, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
            }

            videos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isHindi) "जल्द ही उपलब्ध होगा" else "Coming soon",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHindi) "व्यवस्थापक से वीडियो जोड़ने को कहें" else "Admin can add videos from the panel",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(videos, key = { it.id }) { curated ->
                        val video = Video(
                            id = curated.youtubeVideoId,
                            title = curated.title,
                            titleHi = curated.titleHi,
                            thumbnailUrl = curated.thumbnailUrl.ifBlank {
                                "https://i.ytimg.com/vi/${curated.youtubeVideoId}/mqdefault.jpg"
                            },
                            thumbnailUrlHQ = "https://i.ytimg.com/vi/${curated.youtubeVideoId}/hqdefault.jpg",
                            channelName = curated.channelName,
                            durationFormatted = curated.duration
                        )
                        VideoCard(
                            video = video,
                            onClick = {
                                val intent = Intent(context, PlayerActivity::class.java).apply {
                                    putExtra("videoId", curated.youtubeVideoId)
                                    putExtra("videoTitle", curated.title)
                                }
                                context.startActivity(intent)
                            },
                            width = 180.dp
                        )
                    }
                }
            }
        }
    }
}

@com.google.firebase.firestore.IgnoreExtraProperties
data class CuratedVideo(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val youtubeVideoId: String = "",
    val thumbnailUrl: String = "",
    val channelName: String = "",
    val duration: String = "",
    val priority: Int = 0,
    val active: Boolean = true
)
