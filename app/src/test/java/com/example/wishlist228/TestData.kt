package com.example.wishlist228

import com.example.wishlist228.models.Friend
import com.example.wishlist228.models.Gift
import com.example.wishlist228.models.GiftListItem
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.models.GiftWithTags
import com.example.wishlist228.models.Tag

/** Small helpers that build model objects for the tests. */
internal object TestData {

    fun gift(
        id: Long,
        friendId: Long = 1,
        title: String = "Gift $id",
        description: String = "",
        price: Double = 0.0,
        rating: Int = 3,
        status: GiftStatus = GiftStatus.IDEA,
        createdAt: Long = id
    ) = Gift(
        id = id,
        friendId = friendId,
        title = title,
        description = description,
        price = price,
        rating = rating,
        status = status,
        createdAt = createdAt
    )

    fun item(gift: Gift, friendName: String = "Friend", vararg tags: Tag) =
        GiftListItem(gift, friendName, tags.toList())

    fun withTags(gift: Gift, vararg tags: Tag) = GiftWithTags(gift, tags.toList())

    fun friend(id: Long, name: String, birthdayEpochDay: Long, budget: Double = 0.0) =
        Friend(id = id, name = name, birthdayEpochDay = birthdayEpochDay, budget = budget)
}
