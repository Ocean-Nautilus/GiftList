package com.example.wishlist228

import com.example.wishlist228.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    private fun epoch(year: Int, month: Int, day: Int) = LocalDate.of(year, month, day).toEpochDay()

    @Test
    fun birthdayToday_isZeroDaysAway() {
        val today = LocalDate.of(2026, 10, 14)
        assertEquals(0, DateUtils.daysUntilBirthday(epoch(1995, 10, 14), today))
    }

    @Test
    fun birthdayLaterThisYear_countsDaysInThisYear() {
        val today = LocalDate.of(2026, 10, 14)
        assertEquals(17, DateUtils.daysUntilBirthday(epoch(1990, 10, 31), today))
    }

    @Test
    fun birthdayAlreadyPassed_movesToNextYear() {
        val today = LocalDate.of(2026, 10, 14)
        assertEquals(LocalDate.of(2027, 5, 10), DateUtils.nextBirthday(epoch(1990, 5, 10), today))
        assertEquals(208, DateUtils.daysUntilBirthday(epoch(1990, 5, 10), today))
    }

    @Test
    fun leapDayBirthday_isCelebratedOn28thInCommonYears() {
        val today = LocalDate.of(2025, 2, 20)
        assertEquals(LocalDate.of(2025, 2, 28), DateUtils.nextBirthday(epoch(2000, 2, 29), today))
        assertEquals(8, DateUtils.daysUntilBirthday(epoch(2000, 2, 29), today))
    }

    @Test
    fun turningAge_isAgeOnTheNextBirthday() {
        val today = LocalDate.of(2026, 10, 14)
        assertEquals(37, DateUtils.turningAge(epoch(1990, 5, 10), today))
        assertEquals(31, DateUtils.turningAge(epoch(1995, 10, 14), today))
    }

    @Test
    fun pluralDays_followsRussianRules() {
        assertEquals("день", DateUtils.pluralDays(1))
        assertEquals("дня", DateUtils.pluralDays(2))
        assertEquals("дня", DateUtils.pluralDays(4))
        assertEquals("дней", DateUtils.pluralDays(5))
        assertEquals("дней", DateUtils.pluralDays(11))
        assertEquals("дней", DateUtils.pluralDays(12))
        assertEquals("день", DateUtils.pluralDays(21))
        assertEquals("дня", DateUtils.pluralDays(22))
        assertEquals("дней", DateUtils.pluralDays(100))
    }

    @Test
    fun countdownText_hasSpecialWordsForTodayAndTomorrow() {
        assertEquals("сегодня", DateUtils.countdownText(0))
        assertEquals("завтра", DateUtils.countdownText(1))
        assertEquals("через 3 дня", DateUtils.countdownText(3))
        assertEquals("через 7 дней", DateUtils.countdownText(7))
    }

    @Test
    fun formatFull_usesDayMonthYear() {
        assertEquals("14.10.2026", DateUtils.formatFull(epoch(2026, 10, 14)))
    }
}
