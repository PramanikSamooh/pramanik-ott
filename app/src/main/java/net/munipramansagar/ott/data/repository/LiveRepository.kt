package net.munipramansagar.ott.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.munipramansagar.ott.data.model.LiveStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeLiveStatus(): Flow<LiveStatus> = callbackFlow {
        val listener: ListenerRegistration = firestore
            .collection("live")
            .document("status")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(LiveStatus())
                    return@addSnapshotListener
                }
                val status = snapshot?.toObject(LiveStatus::class.java) ?: LiveStatus()
                trySend(status)
            }

        awaitClose { listener.remove() }
    }
}
