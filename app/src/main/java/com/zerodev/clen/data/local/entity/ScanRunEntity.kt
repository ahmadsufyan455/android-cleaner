package com.zerodev.clen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zerodev.clen.domain.model.ScanStatus

@Entity(tableName = "scan_runs")
data class ScanRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long?,
    val totalBytesFound: Long,
    val status: ScanStatus,
)
