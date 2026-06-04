package com.zerodev.clen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zerodev.clen.data.local.dao.FileItemDao
import com.zerodev.clen.data.local.dao.ScanRunDao
import com.zerodev.clen.data.local.dao.TrashEntryDao
import com.zerodev.clen.data.local.entity.FileItemEntity
import com.zerodev.clen.data.local.entity.ScanRunEntity
import com.zerodev.clen.data.local.entity.TrashEntryEntity

@Database(
    entities = [
        FileItemEntity::class,
        ScanRunEntity::class,
        TrashEntryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(RoomTypeConverters::class)
abstract class ClenDatabase : RoomDatabase() {
    abstract fun fileItemDao(): FileItemDao

    abstract fun scanRunDao(): ScanRunDao

    abstract fun trashEntryDao(): TrashEntryDao
}
