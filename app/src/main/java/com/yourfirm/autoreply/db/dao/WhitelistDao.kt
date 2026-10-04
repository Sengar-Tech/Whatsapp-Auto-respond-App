package com.yourfirm.autoreply.db.dao

import androidx.room.*
import com.yourfirm.autoreply.db.entity.WhitelistEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistDao {

    @Query("SELECT EXISTS(SELECT 1 FROM whitelist WHERE normalizedNumber = :number)")
    suspend fun exists(number: String): Boolean

    @Query("SELECT * FROM whitelist ORDER BY addedAt DESC")
    fun getAll(): Flow<List<WhitelistEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WhitelistEntry)

    @Delete
    suspend fun delete(entry: WhitelistEntry)
}
