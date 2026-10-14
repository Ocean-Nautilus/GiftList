package com.example.wishlist228.database

import androidx.room.TypeConverter
import com.example.wishlist228.models.GiftStatus

/** Room stores enums as plain strings. */
class Converters {

    @TypeConverter
    fun statusToString(status: GiftStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): GiftStatus =
        runCatching { GiftStatus.valueOf(value) }.getOrDefault(GiftStatus.IDEA)
}
