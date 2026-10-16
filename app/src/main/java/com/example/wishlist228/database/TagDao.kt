package com.example.wishlist228.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wishlist228.models.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): Tag?

    /** Returns -1 when a tag with the same name already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    /** Removes tags that are not used by any gift any more. */
    @Query("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tagId FROM gift_tag_cross_ref)")
    suspend fun deleteUnused()

    @Query("DELETE FROM tags")
    suspend fun deleteAll()
}
