package com.example.wishlist228.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.wishlist228.models.Gift
import com.example.wishlist228.models.GiftTagCrossRef
import com.example.wishlist228.models.GiftWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftDao {

    // @Transaction is required for queries that load a @Relation (gift + its tags)
    @Transaction
    @Query("SELECT * FROM gifts")
    fun observeAllWithTags(): Flow<List<GiftWithTags>>

    @Transaction
    @Query("SELECT * FROM gifts WHERE id = :id")
    fun observeByIdWithTags(id: Long): Flow<GiftWithTags?>

    @Transaction
    @Query("SELECT * FROM gifts WHERE id = :id")
    suspend fun getByIdWithTags(id: Long): GiftWithTags?

    @Insert
    suspend fun insert(gift: Gift): Long

    @Update
    suspend fun update(gift: Gift)

    @Query("DELETE FROM gifts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE gifts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM gifts")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(refs: List<GiftTagCrossRef>)

    @Query("DELETE FROM gift_tag_cross_ref WHERE giftId = :giftId")
    suspend fun deleteCrossRefs(giftId: Long)
}
