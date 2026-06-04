package com.zerodev.clen.data.local

import androidx.room.TypeConverter
import com.zerodev.clen.domain.model.JunkCategory
import com.zerodev.clen.domain.model.ScanSource
import com.zerodev.clen.domain.model.ScanStatus

class RoomTypeConverters {
    @TypeConverter
    fun junkCategoryToString(value: JunkCategory): String = value.name

    @TypeConverter
    fun stringToJunkCategory(value: String): JunkCategory = JunkCategory.valueOf(value)

    @TypeConverter
    fun scanStatusToString(value: ScanStatus): String = value.name

    @TypeConverter
    fun stringToScanStatus(value: String): ScanStatus = ScanStatus.valueOf(value)

    @TypeConverter
    fun scanSourceToString(value: ScanSource): String = value.name

    @TypeConverter
    fun stringToScanSource(value: String): ScanSource = ScanSource.valueOf(value)
}
