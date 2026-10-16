package com.example.wishlist228.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/** A gift together with all its tags (loaded through the junction table). */
data class GiftWithTags(
    @Embedded val gift: Gift,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GiftTagCrossRef::class,
            parentColumn = "giftId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)
