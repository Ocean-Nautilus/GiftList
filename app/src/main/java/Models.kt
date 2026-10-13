package com.example.wishlist228

import java.util.UUID

enum class WishPriority(val label: String, val emoji: String) {
    HIGH("Очень хочу", "🔥"),
    MEDIUM("Буду рад", "✨"),
    LOW("По ситуации", "💭")
}

data class WishItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val price: String? = null,
    val link: String? = null,
    val imageUrl: String? = null,
    val category: String = "Основной",
    val priority: WishPriority = WishPriority.MEDIUM,
    val isReserved: Boolean = false,
    val reservedByName: String? = null
)

data class WishCategory(
    val id: String,
    val name: String,
    val isPrivate: Boolean = false
)

data class Friend(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val birthday: String,
    val daysUntilBirthday: Int,
    val wishCount: Int
)

data class IdeaCollection(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String
)