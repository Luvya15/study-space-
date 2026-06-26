package com.example.data.repository

import com.example.data.database.JournalDao
import com.example.data.database.UserStatsDao
import com.example.data.model.JournalEntry
import com.example.data.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart

class StudyRepository(
    private val journalDao: JournalDao,
    private val userStatsDao: UserStatsDao
) {
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()
    val userStats: Flow<UserStats?> = userStatsDao.getUserStatsFlow()
        .onStart {
            // Seed default data if database is empty
            seedInitialDataIfEmpty()
        }

    suspend fun getLatestStats(): UserStats {
        seedInitialDataIfEmpty()
        return userStatsDao.getUserStats() ?: UserStats()
    }

    suspend fun insertJournalEntry(entry: JournalEntry) {
        journalDao.insertEntry(entry)
    }

    suspend fun updateUserStats(stats: UserStats) {
        userStatsDao.insertOrUpdate(stats)
    }

    private suspend fun seedInitialDataIfEmpty() {
        val currentStats = userStatsDao.getUserStats()
        if (currentStats == null) {
            // Seed initial statistics
            userStatsDao.insertOrUpdate(
                UserStats(
                    id = 1,
                    streakDays = 12,
                    inkDrops = 742,
                    dailyAverageMinutes = 140,
                    totalFocusHours = 84,
                    roomsVisited = 12,
                    username = "Arlo",
                    isDarkTheme = false
                )
            )

            // Seed initial journal entries to match the mockup
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            
            journalDao.insertEntry(
                JournalEntry(
                    nookName = "The Oak Library",
                    microIntention = "Drafting Intro",
                    durationMinutes = 50,
                    timestamp = now - (1 * oneDayMs), // 1 day ago
                    subject = "Design"
                )
            )
            journalDao.insertEntry(
                JournalEntry(
                    nookName = "Botanical Atrium",
                    microIntention = "Reading Chapter 4",
                    durationMinutes = 90,
                    timestamp = now - (2 * oneDayMs), // 2 days ago
                    subject = "Physics"
                )
            )
            journalDao.insertEntry(
                JournalEntry(
                    nookName = "Quiet Corner",
                    microIntention = "Email Processing",
                    durationMinutes = 25,
                    timestamp = now - (3 * oneDayMs), // 3 days ago
                    subject = "Mathematics"
                )
            )
        }
    }
}
