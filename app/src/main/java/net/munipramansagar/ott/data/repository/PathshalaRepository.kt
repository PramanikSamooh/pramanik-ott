package net.munipramansagar.ott.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.model.PathshalaClass
import net.munipramansagar.ott.data.model.Teacher
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PathshalaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getActiveClasses(): List<PathshalaClass> {
        return try {
            val snapshot = firestore
                .collection("pathshala")
                .document("classes")
                .collection("items")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PathshalaClass::class.java)?.copy(id = doc.id)
            }.filter { it.active }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getTeachers(): List<Teacher> {
        return try {
            val snapshot = firestore
                .collection("pathshala")
                .document("teachers")
                .collection("items")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Teacher::class.java)?.copy(id = doc.id)
            }.filter { it.active }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getTodaysClasses(): List<PathshalaClass> {
        val today = getCurrentDayOfWeek()
        return getActiveClasses().filter { today in it.dayOfWeek }
            .sortedBy { it.time }
    }

    suspend fun getUpcomingClasses(): List<PathshalaClass> {
        val today = getCurrentDayOfWeek()
        val currentTime = getCurrentTimeHHmm()
        return getActiveClasses()
            .filter { today in it.dayOfWeek && it.time >= currentTime }
            .sortedBy { it.time }
    }

    private fun getCurrentDayOfWeek(): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        // Calendar: SUNDAY=1..SATURDAY=7. We need 0=Sunday..6=Saturday
        return cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    private fun getCurrentTimeHHmm(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }
}
