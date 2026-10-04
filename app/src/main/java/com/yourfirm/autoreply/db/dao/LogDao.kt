package com.yourfirm.autoreply.db.dao

import androidx.room.*
import com.yourfirm.autoreply.db.entity.LogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Query("SELECT * FROM log ORDER BY timestamp DESC LIMIT 100")
    fun getRecent(): Flow<List<LogEntry>>

    @Insert
    suspend fun insert(entry: LogEntry)

    @Query("SELECT COUNT(*) FROM log WHERE status = 'sent' AND timestamp > :since")
    suspend fun countSentSince(since: Long): Int
}
