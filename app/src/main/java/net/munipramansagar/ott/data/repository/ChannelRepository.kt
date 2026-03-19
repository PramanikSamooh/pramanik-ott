package net.munipramansagar.ott.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.model.Channel
import net.munipramansagar.ott.data.model.HomeRow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getChannels(): List<Channel> {
        return firestore.collection("channels")
            .orderBy("priority")
            .get()
            .await()
            .toObjects(Channel::class.java)
    }

    suspend fun getHomeRows(): List<HomeRow> {
        return firestore.collection("homeRows")
            .orderBy("priority")
            .get()
            .await()
            .toObjects(HomeRow::class.java)
    }
}
