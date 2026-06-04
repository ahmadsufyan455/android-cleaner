package com.zerodev.clen.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zerodev.clen.data.local.entity.FileItemEntity
import com.zerodev.clen.domain.model.JunkCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface FileItemDao {
    @Query("SELECT * FROM file_items ORDER BY sizeBytes DESC")
    fun observeAll(): Flow<List<FileItemEntity>>

    @Query("SELECT * FROM file_items WHERE category = :category ORDER BY sizeBytes DESC")
    fun observeByCategory(category: JunkCategory): Flow<List<FileItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<FileItemEntity>)

    @Query("DELETE FROM file_items")
    suspend fun deleteAll()

    @Query("DELETE FROM file_items WHERE uri IN (:uris)")
    suspend fun deleteByUris(uris: List<String>)
}
