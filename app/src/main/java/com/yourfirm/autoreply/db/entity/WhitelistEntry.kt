package com.yourfirm.autoreply.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist")
data class WhitelistEntry(
    @PrimaryKey val normalizedNumber: String,
    val displayLabel: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
