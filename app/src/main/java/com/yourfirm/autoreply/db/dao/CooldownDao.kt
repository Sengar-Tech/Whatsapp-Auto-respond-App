package com.yourfirm.autoreply.db.dao

import androidx.room.*
import com.yourfirm.autoreply.db.entity.CooldownEntry

@Dao
interface CooldownDao {

    @Query("SELECT * FROM cooldown WHERE normalizedNumber = :number LIMIT 1")
    suspend fun getLastReply(number: String): CooldownEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: CooldownEntry)

    @Query("DELETE FROM cooldown WHERE timestamp < :cutoff")
    suspend fun cleanup(cutoff: Long)
}
