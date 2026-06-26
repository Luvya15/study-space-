package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nookName: String,
    val microIntention: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String
)
