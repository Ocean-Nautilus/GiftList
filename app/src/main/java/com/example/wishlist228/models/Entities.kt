package com.example.wishlist228.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A person we choose gifts for.
 * [birthdayEpochDay] is the birth date as `LocalDate.toEpochDay()`.
 * [budget] is the amount we plan to spend on this person per year.
 */
@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val birthdayEpochDay: Long,
    val budget: Double = 0.0,
    val notes: String = ""
)

/**
 * The main entity of the app: a gift idea (or an already bought gift) for a friend.
 * One friend has many gifts (one-to-many). When a friend is deleted, all his gifts are
 * deleted too ([ForeignKey.CASCADE]).
 */
@Entity(
    tableName = "gifts",
    foreignKeys = [
        ForeignKey(
            entity = Friend::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("friendId")]
)
data class Gift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendId: Long,
    val title: String,
    val description: String = "",
    val price: Double = 0.0,
    val link: String = "",
    val imageUrl: String = "",
    /** How much we want to give it: 1..5 stars. */
    val rating: Int = 3,
    val status: GiftStatus = GiftStatus.IDEA,
    val createdAt: Long = System.currentTimeMillis()
)

/** A label such as "birthday" or "sport". Tags are shared between gifts. */
@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

/**
 * Junction table for the many-to-many relation between gifts and tags:
 * a gift may have several tags, a tag may be used by several gifts.
 */
@Entity(
    tableName = "gift_tag_cross_ref",
    primaryKeys = ["giftId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Gift::class,
            parentColumns = ["id"],
            childColumns = ["giftId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tagId")]
)
data class GiftTagCrossRef(
    val giftId: Long,
    val tagId: Long
)
