package net.munipramansagar.ott.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class TvSession(
    val code: String = "",
    val deviceId: String = "",
    val linkedUid: String = "",
    val linkedEmail: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "pending" // pending, linked, expired
)

@Singleton
class TvLinkRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val sessionsRef = firestore.collection("tv_sessions")

    /**
     * Generate a 6-digit code and create a session in Firestore.
     * Returns the code.
     */
    suspend fun createSession(deviceId: String): String {
        // Generate unique 6-digit code
        val code = (100000 + Random.nextInt(900000)).toString()

        val session = TvSession(
            code = code,
            deviceId = deviceId,
            createdAt = System.currentTimeMillis(),
            status = "pending"
        )

        sessionsRef.document(code).set(session).await()
        return code
    }

    /**
     * TV listens for when the session gets linked by the phone.
     * Returns a Flow that emits the session state.
     */
    fun observeSession(code: String): Flow<TvSession?> = callbackFlow {
        val listener: ListenerRegistration = sessionsRef.document(code)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val session = snapshot?.toObject(TvSession::class.java)
                trySend(session)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Phone links a session by writing its uid and email.
     */
    suspend fun linkSession(code: String, uid: String, email: String): Boolean {
        return try {
            val doc = sessionsRef.document(code).get().await()
            if (!doc.exists()) return false
            val session = doc.toObject(TvSession::class.java) ?: return false
            if (session.status != "pending") return false

            sessionsRef.document(code).update(
                mapOf(
                    "linkedUid" to uid,
                    "linkedEmail" to email,
                    "status" to "linked"
                )
            ).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Clean up expired sessions (older than 10 minutes).
     */
    suspend fun cleanupExpiredSessions() {
        try {
            val cutoff = System.currentTimeMillis() - 10 * 60 * 1000
            val expired = sessionsRef
                .whereLessThan("createdAt", cutoff)
                .get().await()
            for (doc in expired) {
                doc.reference.delete()
            }
        } catch (_: Exception) {}
    }
}
