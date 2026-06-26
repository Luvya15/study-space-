package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.JournalEntry
import com.example.data.model.UserStats
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StudyTab {
    LIBRARY, SANCTUARY, CHRONICLE
}

data class Nook(
    val name: String,
    val category: String,
    val ambientSound: String,
    val presentCount: Int,
    val maxPresent: Int,
    val imageUrl: String,
    val iconName: String,
    val isFeatured: Boolean = false
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = StudyRepository(db.journalDao(), db.userStatsDao())

    // Database flow streams
    val allJournalEntries: StateFlow<List<JournalEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI Tab State
    var currentTab by mutableStateOf(StudyTab.LIBRARY)
        private set

    // Selected Nook Room
    var activeNook by mutableStateOf<Nook?>(null)
        private set

    // Room Search Query
    var searchQuery by mutableStateOf("")

    // List of Nooks from mockups
    val availableNooks = listOf(
        Nook(
            name = "Premier Institute",
            category = "IIT & AIIMS Inspired",
            ambientSound = "Ancient Echoes",
            presentCount = 0,
            maxPresent = 6,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuANHMC311OGr6L7mwy7MDVcWj1Opah--4O-7eLAMv2OfawSq6-oqNLQDXG3LZv9JUhcNgNkY47j1vrUbV0uH__NCLuzlXEhgdz1TcNTb3gpmTu8aoNYr2rhc0FTr2cBkD6ehKBpJwsGNpip8AjyZV-Nxjb30xBAMALG8SFWflK5arRTJXfC9Xywa6HvSv-RmpOYRtYdrbmVDDmk5Pkiiq5hJN7JwvGvsvFAe25exXwXsIYP3bcKpxBcofzVIl4shz6lkeDXvgfCoNgp",
            iconName = "school"
        ),
        Nook(
            name = "The Oak Library",
            category = "Vintage Academia",
            ambientSound = "Rain & Crackle",
            presentCount = 4,
            maxPresent = 6,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBUD9IwSOxOz0v4z06hHb5nDRpWgUBNH8slgNEmCd0hG0J_KNQ0nKB02u-Tyl7mhvNJx7_wW_YY3qV92tKvlDPUMSARZH9xlfYPbrEND6lIbK0Z2gBoji9RnXT2gmA2USUNR8CQcTxKoXVv0_zzRBX3KxfXkgd_zlGnryK4cD2uDO3fG_y06JVRwqfrz7dbMvI5qwPX1-GSOnjmCFTcJ_hqTWva0TTe1ru73TQc9aiIs8MWmhmUnnDiuGKuhqFl0yQFjoHlEvX4Mty1",
            iconName = "menu_book"
        ),
        Nook(
            name = "Botanical Atrium",
            category = "Nature / Glasshouse",
            ambientSound = "Forest Stream",
            presentCount = 12,
            maxPresent = 20,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCzZHr6cOwzo_wLuu9QCg_ru8H21IeapVJhoR9kCWfRRgHi0oEItGqjIs43GLHo_0CDDTFYdNY2RtKng2RoB5d_6A9r2lBKTCV3HdCwKTX6_zQMVXo_v6jfyx42O4L96zbjGAV1ixLMnqA2xiZuxZdd-mRWPlfKR6sUQv6V_rSwDRbVArjn9Kb7wdYInJIuqnVsI3Q0raW4ayOwLba2Nkr_ngb9K_4jNggnyJbb75Y90F3LgJrTznsDfjvf1YV_G8B0v3ZVqLFC2EE9",
            iconName = "local_florist"
        ),
        Nook(
            name = "Vinyl Corner",
            category = "Retro / Lo-fi",
            ambientSound = "Lo-Fi Beats",
            presentCount = 2,
            maxPresent = 4,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6MXlMUe0CokmVWSu0jAL8yr53tMyZ_bWoBvUPAsMbfRlvQEwO-6tvZVZVTuUgIAiZ5ffyx1Uz_g3VjNyfzr5NhsPjum3qHUOCPxfiIpa9zl3rO911nX49lBXdeV0HM_0ZbAmKQXHoYPzkXF4UOm89QkMMFW4YuPToe09Xdni2MOleO6jasMAo94aOTJRIkGbSEg-kuva3cJ3arLzXvEtPhxTVWked5a95MrQgUFlQ43TNFXGhriU_4XvESuHz_GlV0slDBq-dtRNX",
            iconName = "album"
        ),
        Nook(
            name = "Midnight Study",
            category = "Dark / Deep Focus",
            ambientSound = "White Noise",
            presentCount = 1,
            maxPresent = 2,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDcp1o5jarN5BYyGyS2gG4fnvV4TZd32R9IEjOaGSkRdis3JWJ5iqB_V33jVW6btKfAW9fUSCa4Dj1gP_yiVwO853r86PFIwvOHlcRrQjPeYbvOqBSgZqCbx_mhi4mhiYujQOh1Cv34XtEuxoHsqVkVgRiuMg3xESCAIMxrAW8LvGrn0C1q_ulVmlQ2Y40sd6jwCcQQImVnw3ZFBotQB8EWnRS7ILqiRRPoUVpjbE2vHKKXrkT0tFjPpuioNefUAu1OXbBlgJuONArK",
            iconName = "nightlight",
            isFeatured = true
        )
    )

    // Filtered Nooks list based on search bar
    fun getFilteredNooks(): List<Nook> {
        val query = searchQuery.trim()
        if (query.isEmpty()) return availableNooks
        return availableNooks.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.ambientSound.contains(query, ignoreCase = true)
        }
    }

    // Active Timer Variables
    var timerSecondsRemaining by mutableIntStateOf(25 * 60)
        private set
    var isTimerRunning by mutableStateOf(false)
        private set
    private var timerJob: Job? = null

    // Ambient mixer values
    var volumeLofi by mutableFloatStateOf(0.40f)
    var volumeRain by mutableFloatStateOf(0.75f)
    var volumeCafe by mutableFloatStateOf(0.15f)

    // Current Session Study details
    var microIntention by mutableStateOf("Drafting Intro")
    var selectedSubject by mutableStateOf("Design") // Math, Physics, Design, General

    // Completed Session Summary State
    var showSessionSummary by mutableStateOf(false)
        private set
    var sessionEarnedDrops by mutableIntStateOf(15)
        private set
    var sessionCompletedDuration by mutableIntStateOf(25)
        private set

    fun selectTab(tab: StudyTab) {
        currentTab = tab
    }

    fun selectNook(nook: Nook?) {
        activeNook = nook
        if (nook != null) {
            // Reset timer for the entered room
            resetTimer()
            currentTab = StudyTab.SANCTUARY
        }
    }

    // Timer Actions
    fun toggleTimer() {
        if (isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        isTimerRunning = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timerSecondsRemaining > 0) {
                delay(1000)
                timerSecondsRemaining--
            }
            onTimerComplete()
        }
    }

    fun pauseTimer() {
        isTimerRunning = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        timerSecondsRemaining = 25 * 60
    }

    fun skipTimer() {
        // Fast forward tool for demo/playtesting so users can see screens instantly
        pauseTimer()
        timerSecondsRemaining = 5 // set to 5 seconds remaining
        startTimer()
    }

    private fun onTimerComplete() {
        isTimerRunning = false
        timerJob?.cancel()

        val completedMinutes = 25
        sessionCompletedDuration = completedMinutes
        sessionEarnedDrops = 15

        viewModelScope.launch {
            // 1. Save entry to local SQLite Journal Database
            val entry = JournalEntry(
                nookName = activeNook?.name ?: "Quiet Sanctuary",
                microIntention = microIntention,
                durationMinutes = completedMinutes,
                subject = selectedSubject
            )
            repository.insertJournalEntry(entry)

            // 2. Update user stats locally in SQLite
            val currentStats = repository.getLatestStats()
            val newStats = currentStats.copy(
                streakDays = currentStats.streakDays + 1,
                inkDrops = currentStats.inkDrops + sessionEarnedDrops,
                dailyAverageMinutes = ((currentStats.dailyAverageMinutes * currentStats.streakDays) + completedMinutes) / (currentStats.streakDays + 1),
                totalFocusHours = currentStats.totalFocusHours + 1, // simplified addition
                roomsVisited = currentStats.roomsVisited + 1
            )
            repository.updateUserStats(newStats)

            // 3. Show focus complete overlay summary screen
            showSessionSummary = true
        }
    }

    fun closeSummary() {
        showSessionSummary = false
        activeNook = null
        currentTab = StudyTab.LIBRARY
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val stats = repository.getLatestStats()
            repository.updateUserStats(stats.copy(isDarkTheme = !stats.isDarkTheme))
        }
    }

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            val stats = repository.getLatestStats()
            repository.updateUserStats(stats.copy(username = newName))
        }
    }
}
