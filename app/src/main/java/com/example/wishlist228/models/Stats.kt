package com.example.wishlist228.models

/** Budget and spent money of one friend (a pair of bars on the chart). */
data class FriendBudget(
    val name: String,
    val budget: Double,
    val spent: Double
)

/** Everything the statistics screen shows, calculated from the real data of the user. */
data class StatsUiState(
    val isLoading: Boolean = true,
    val totalFriends: Int = 0,
    val totalGifts: Int = 0,
    /** Money spent on gifts that are already bought or given. */
    val totalSpent: Double = 0.0,
    /** Money planned for ideas that are not bought yet. */
    val plannedSpend: Double = 0.0,
    val averageRating: Double = 0.0,
    val statusCounts: Map<GiftStatus, Int> = emptyMap(),
    val friendBudgets: List<FriendBudget> = emptyList(),
    val nextBirthdayName: String? = null,
    val nextBirthdayDays: Int? = null,
    val currencySymbol: String = "₽"
)
