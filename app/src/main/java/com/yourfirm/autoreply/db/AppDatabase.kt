package com.yourfirm.autoreply.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yourfirm.autoreply.db.dao.CooldownDao
import com.yourfirm.autoreply.db.dao.LogDao
import com.yourfirm.autoreply.db.dao.WhitelistDao
import com.yourfirm.autoreply.db.entity.CooldownEntry
import com.yourfirm.autoreply.db.entity.LogEntry
import com.yourfirm.autoreply.db.entity.WhitelistEntry

/**
 * Single Room database for the entire app.
 * Stub entities and DAOs live in sub-packages — fully implemented in Phase 2.
 */
@Database(
    entities = [WhitelistEntry::class, CooldownEntry::class, LogEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun whitelistDao(): WhitelistDao
    abstract fun cooldownDao(): CooldownDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autoreply.db"
                ).build().also { instance = it }
            }
    }
}
