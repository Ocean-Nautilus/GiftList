package com.example.wishlist228.utils

import com.example.wishlist228.models.Friend
import com.example.wishlist228.models.FriendBudget
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.models.GiftWithTags
import com.example.wishlist228.models.StatsUiState
import java.time.LocalDate

/** Turns friends and gifts into the numbers and chart data of the statistics screen. */
object StatsCalculator {

    fun calculate(
        friends: List<Friend>,
        gifts: List<GiftWithTags>,
        currencySymbol: String,
        today: LocalDate = LocalDate.now()
    ): StatsUiState {
        val allGifts = gifts.map { it.gift }
        // A gift counts as "spent" once it is bought or given
        val bought = allGifts.filter { it.status != GiftStatus.IDEA }

        val budgets = friends.map { friend ->
            FriendBudget(
                name = friend.name,
                budget = friend.budget,
                spent = bought.filter { it.friendId == friend.id }.sumOf { it.price }
            )
        }

        val nearest = friends.minByOrNull { DateUtils.daysUntilBirthday(it.birthdayEpochDay, today) }

        return StatsUiState(
            isLoading = false,
            totalFriends = friends.size,
            totalGifts = allGifts.size,
            totalSpent = bought.sumOf { it.price },
            plannedSpend = allGifts.filter { it.status == GiftStatus.IDEA }.sumOf { it.price },
            averageRating = if (allGifts.isEmpty()) 0.0 else allGifts.map { it.rating }.average(),
            statusCounts = GiftStatus.entries.associateWith { status ->
                allGifts.count { it.status == status }
            },
            friendBudgets = budgets,
            nextBirthdayName = nearest?.name,
            nextBirthdayDays = nearest?.let { DateUtils.daysUntilBirthday(it.birthdayEpochDay, today) },
            currencySymbol = currencySymbol
        )
    }
}
