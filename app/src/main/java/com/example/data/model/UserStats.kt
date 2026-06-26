package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val streakDays: Int = 12,
    val inkDrops: Int = 742,
    val dailyAverageMinutes: Int = 140,
    val totalFocusHours: Int = 84,
    val roomsVisited: Int = 12,
    val username: String = "Arlo",
    val isDarkTheme: Boolean = false
)
