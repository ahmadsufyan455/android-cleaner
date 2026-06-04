package com.zerodev.clen.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zerodev.clen.data.local.entity.ScanRunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanRunDao {
    @Query("SELECT * FROM scan_runs ORDER BY startedAt DESC LIMIT 1")
    fun observeLatest(): Flow<ScanRunEntity?>

    @Insert
    suspend fun insert(scanRun: ScanRunEntity): Long

    @Update
    suspend fun update(scanRun: ScanRunEntity)

    @Query("DELETE FROM scan_runs")
    suspend fun deleteAll()
}
