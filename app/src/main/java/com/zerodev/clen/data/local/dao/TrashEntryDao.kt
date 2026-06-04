package com.zerodev.clen.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zerodev.clen.data.local.entity.TrashEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashEntryDao {
    @Query("SELECT * FROM trash_entries ORDER BY deletedAt DESC")
    fun observeAll(): Flow<List<TrashEntryEntity>>

    @Query("SELECT * FROM trash_entries WHERE expiresAt <= :nowMillis")
    suspend fun getExpired(nowMillis: Long): List<TrashEntryEntity>

    @Query("SELECT * FROM trash_entries")
    suspend fun getAll(): List<TrashEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: TrashEntryEntity)

    @Query("DELETE FROM trash_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM trash_entries")
    suspend fun deleteAll()

    @Query("DELETE FROM trash_entries WHERE expiresAt <= :nowMillis")
    suspend fun deleteExpired(nowMillis: Long)
}
