package com.example.wishlist228.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wishlist228.models.Friend
import com.example.wishlist228.models.Gift
import com.example.wishlist228.models.GiftTagCrossRef
import com.example.wishlist228.models.Tag

/**
 * The SQLite database of the app (through Room). Four tables:
 * friends, gifts, tags and the junction table gift_tag_cross_ref.
 * Room turns on `PRAGMA foreign_keys` by itself, so the CASCADE rules work.
 */
@Database(
    entities = [Friend::class, Gift::class, Tag::class, GiftTagCrossRef::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun friendDao(): FriendDao
    abstract fun giftDao(): GiftDao
    abstract fun tagDao(): TagDao

    companion object {
        private const val DATABASE_NAME = "giftlist.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .build()
    }
}
