package com.yourfirm.autoreply.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cooldown")
data class CooldownEntry(
    @PrimaryKey val normalizedNumber: String,
    val timestamp: Long
)
