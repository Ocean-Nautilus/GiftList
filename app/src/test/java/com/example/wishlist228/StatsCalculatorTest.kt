package com.example.wishlist228

import com.example.wishlist228.TestData.friend
import com.example.wishlist228.TestData.gift
import com.example.wishlist228.TestData.withTags
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.utils.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class StatsCalculatorTest {

    private val today = LocalDate.of(2026, 10, 14)

    private val anna = friend(1, "Анна", LocalDate.of(1996, 10, 20).toEpochDay(), budget = 6000.0)
    private val maxim = friend(2, "Максим", LocalDate.of(1993, 12, 1).toEpochDay(), budget = 9000.0)

    private val gifts = listOf(
        withTags(gift(1, friendId = 1, price = 4500.0, rating = 5, status = GiftStatus.PURCHASED)),
        withTags(gift(2, friendId = 1, price = 1200.0, rating = 3, status = GiftStatus.IDEA)),
        withTags(gift(3, friendId = 2, price = 2500.0, rating = 4, status = GiftStatus.GIVEN)),
        withTags(gift(4, friendId = 2, price = 7500.0, rating = 4, status = GiftStatus.IDEA))
    )

    @Test
    fun totals_countOnlyBoughtGiftsAsSpent() {
        val stats = StatsCalculator.calculate(listOf(anna, maxim), gifts, "₽", today)
        assertEquals(2, stats.totalFriends)
        assertEquals(4, stats.totalGifts)
        assertEquals(7000.0, stats.totalSpent, 0.0001)
        assertEquals(8700.0, stats.plannedSpend, 0.0001)
        assertEquals(4.0, stats.averageRating, 0.0001)
    }

    @Test
    fun statusCounts_containEveryStatus() {
        val stats = StatsCalculator.calculate(listOf(anna, maxim), gifts, "₽", today)
        assertEquals(2, stats.statusCounts[GiftStatus.IDEA])
        assertEquals(1, stats.statusCounts[GiftStatus.PURCHASED])
        assertEquals(1, stats.statusCounts[GiftStatus.GIVEN])
    }

    @Test
    fun friendBudgets_compareSpentWithBudget() {
        val stats = StatsCalculator.calculate(listOf(anna, maxim), gifts, "₽", today)
        assertEquals(4500.0, stats.friendBudgets[0].spent, 0.0001)
        assertEquals(6000.0, stats.friendBudgets[0].budget, 0.0001)
        assertEquals(2500.0, stats.friendBudgets[1].spent, 0.0001)
    }

    @Test
    fun nextBirthday_isTheNearestOne() {
        val stats = StatsCalculator.calculate(listOf(anna, maxim), gifts, "₽", today)
        assertEquals("Анна", stats.nextBirthdayName)
        assertEquals(6, stats.nextBirthdayDays)
    }

    @Test
    fun noData_givesZerosAndNoBirthday() {
        val stats = StatsCalculator.calculate(emptyList(), emptyList(), "$", today)
        assertEquals(0, stats.totalGifts)
        assertEquals(0.0, stats.averageRating, 0.0001)
        assertNull(stats.nextBirthdayName)
        assertNull(stats.nextBirthdayDays)
        assertEquals("$", stats.currencySymbol)
    }
}
