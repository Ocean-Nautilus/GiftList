package com.example.wishlist228.database

import androidx.room.withTransaction
import com.example.wishlist228.models.Friend
import com.example.wishlist228.models.Gift
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.models.GiftTagCrossRef
import com.example.wishlist228.models.GiftWithTags
import com.example.wishlist228.models.Tag
import kotlinx.coroutines.flow.Flow

/**
 * The single place where ViewModels (and the reminder worker) talk to the database.
 * All functions are `suspend` or return a [Flow]: Room runs them off the main thread.
 */
class GiftRepository(private val database: AppDatabase) {

    private val friendDao = database.friendDao()
    private val giftDao = database.giftDao()
    private val tagDao = database.tagDao()

    // ---------- Reading (reactive: the UI is updated after every change) ----------

    val friends: Flow<List<Friend>> = friendDao.observeAll()
    val gifts: Flow<List<GiftWithTags>> = giftDao.observeAllWithTags()
    val tags: Flow<List<Tag>> = tagDao.observeAll()

    fun observeGift(id: Long): Flow<GiftWithTags?> = giftDao.observeByIdWithTags(id)

    suspend fun getGift(id: Long): GiftWithTags? = giftDao.getByIdWithTags(id)

    // ---------- Friends ----------

    /** Inserts a new friend (id == 0) or updates an existing one. */
    suspend fun saveFriend(friend: Friend): Long =
        if (friend.id == 0L) {
            friendDao.insert(friend)
        } else {
            friendDao.update(friend)
            friend.id
        }

    /** Deletes the friend; his gifts and their tag links are removed by CASCADE. */
    suspend fun deleteFriend(friend: Friend) {
        database.withTransaction {
            friendDao.delete(friend)
            tagDao.deleteUnused()
        }
    }

    // ---------- Gifts ----------

    /**
     * Creates or updates a gift together with its tags in one transaction.
     * [tagNames] replace the previous tags of the gift; unknown tags are created.
     */
    suspend fun saveGift(gift: Gift, tagNames: List<String>): Long =
        database.withTransaction {
            val giftId = if (gift.id == 0L) {
                giftDao.insert(gift)
            } else {
                giftDao.update(gift)
                gift.id
            }
            giftDao.deleteCrossRefs(giftId)
            val refs = tagNames.map { name -> GiftTagCrossRef(giftId, findOrCreateTag(name)) }
            if (refs.isNotEmpty()) giftDao.insertCrossRefs(refs)
            tagDao.deleteUnused()
            giftId
        }

    suspend fun deleteGift(id: Long) {
        database.withTransaction {
            giftDao.deleteById(id)
            tagDao.deleteUnused()
        }
    }

    suspend fun updateGiftStatus(id: Long, status: GiftStatus) {
        giftDao.updateStatus(id, status.name)
    }

    // ---------- Whole database ----------

    /** Removes every friend, gift and tag. */
    suspend fun clearAll() {
        database.withTransaction {
            giftDao.deleteAll()
            friendDao.deleteAll()
            tagDao.deleteAll()
        }
    }

    private suspend fun findOrCreateTag(name: String): Long {
        tagDao.findByName(name)?.let { return it.id }
        val newId = tagDao.insert(Tag(name = name))
        // -1 means that somebody has created the same tag in the meantime
        return if (newId != -1L) newId else checkNotNull(tagDao.findByName(name)).id
    }
}
