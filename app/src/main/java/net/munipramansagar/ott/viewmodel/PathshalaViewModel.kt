package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.model.PathshalaClass
import net.munipramansagar.ott.data.model.Teacher
import net.munipramansagar.ott.data.repository.PathshalaRepository
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

data class PathshalaUiState(
    val isLoading: Boolean = true,
    val allClasses: List<PathshalaClass> = emptyList(),
    val todaysClasses: List<PathshalaClass> = emptyList(),
    val teachers: Map<String, Teacher> = emptyMap(),
    val classesByDay: Map<Int, List<PathshalaClass>> = emptyMap(),
    val selectedLanguage: String? = null, // null = All
    val currentDayOfWeek: Int = 0,
    val currentTime: String = "00:00",
    val error: String? = null
)

@HiltViewModel
class PathshalaViewModel @Inject constructor(
    private val pathshalaRepository: PathshalaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PathshalaUiState())
    val uiState: StateFlow<PathshalaUiState> = _uiState.asStateFlow()

    init {
        loadPathshala()
    }

    private fun loadPathshala() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val classesDeferred = async { pathshalaRepository.getActiveClasses() }
                val teachersDeferred = async { pathshalaRepository.getTeachers() }

                val classes = classesDeferred.await()
                val teachersList = teachersDeferred.await()
                val teachersMap = teachersList.associateBy { it.id }

                val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
                val currentDay = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday
                val currentTime = String.format(
                    "%02d:%02d",
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE)
                )

                val todaysClasses = classes
                    .filter { it.dayOfWeek == currentDay }
                    .sortedBy { it.time }

                val classesByDay = classes
                    .groupBy { it.dayOfWeek }
                    .toSortedMap()
                    .mapValues { (_, v) -> v.sortedBy { it.time } }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allClasses = classes,
                    todaysClasses = todaysClasses,
                    teachers = teachersMap,
                    classesByDay = classesByDay,
                    currentDayOfWeek = currentDay,
                    currentTime = currentTime
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load classes"
                )
            }
        }
    }

    fun setLanguageFilter(language: String?) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun refresh() {
        loadPathshala()
    }

    fun isClassLive(pathshalaClass: PathshalaClass): Boolean {
        val state = _uiState.value
        if (pathshalaClass.dayOfWeek != state.currentDayOfWeek) return false
        val classMinutes = timeToMinutes(pathshalaClass.time)
        val currentMinutes = timeToMinutes(state.currentTime)
        return (currentMinutes - classMinutes) in -15..15
    }

    fun isClassUpcoming(pathshalaClass: PathshalaClass): Boolean {
        val state = _uiState.value
        if (pathshalaClass.dayOfWeek != state.currentDayOfWeek) return false
        val classMinutes = timeToMinutes(pathshalaClass.time)
        val currentMinutes = timeToMinutes(state.currentTime)
        return classMinutes > currentMinutes
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    }

    fun getFilteredClassesByDay(): Map<Int, List<PathshalaClass>> {
        val state = _uiState.value
        val filtered = if (state.selectedLanguage != null) {
            state.allClasses.filter { it.language.equals(state.selectedLanguage, ignoreCase = true) }
        } else {
            state.allClasses
        }
        return filtered
            .groupBy { it.dayOfWeek }
            .toSortedMap()
            .mapValues { (_, v) -> v.sortedBy { it.time } }
    }

    fun getFilteredTodaysClasses(): List<PathshalaClass> {
        val state = _uiState.value
        val todaysFiltered = if (state.selectedLanguage != null) {
            state.todaysClasses.filter { it.language.equals(state.selectedLanguage, ignoreCase = true) }
        } else {
            state.todaysClasses
        }
        return todaysFiltered
    }
}
