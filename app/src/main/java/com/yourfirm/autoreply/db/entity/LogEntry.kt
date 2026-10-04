package com.yourfirm.autoreply.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val timestamp: Long,
    val status: String
)
