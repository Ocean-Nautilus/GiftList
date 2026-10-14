package com.example.wishlist228.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Helpers for birthdays and for showing dates in Russian. */
object DateUtils {

    private val fullFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))

    /** The next date (today or later) on which the birthday is celebrated. */
    fun nextBirthday(birthdayEpochDay: Long, today: LocalDate = LocalDate.now()): LocalDate {
        val birthday = LocalDate.ofEpochDay(birthdayEpochDay)
        // withYear moves 29 February to 28 February in non-leap years
        val thisYear = birthday.withYear(today.year)
        return if (thisYear.isBefore(today)) birthday.withYear(today.year + 1) else thisYear
    }

    /** How many days are left until the next birthday (0 = today). */
    fun daysUntilBirthday(birthdayEpochDay: Long, today: LocalDate = LocalDate.now()): Int =
        ChronoUnit.DAYS.between(today, nextBirthday(birthdayEpochDay, today)).toInt()

    /** The age the person turns on the next birthday. */
    fun turningAge(birthdayEpochDay: Long, today: LocalDate = LocalDate.now()): Int =
        nextBirthday(birthdayEpochDay, today).year - LocalDate.ofEpochDay(birthdayEpochDay).year

    fun formatFull(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(fullFormatter)

    fun formatDayMonth(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(dayMonthFormatter)

    /** Russian plural form of the word "day" for the number [n]. */
    fun pluralDays(n: Int): String {
        val lastTwo = n % 100
        val last = n % 10
        return when {
            lastTwo in 11..14 -> "дней"
            last == 1 -> "день"
            last in 2..4 -> "дня"
            else -> "дней"
        }
    }

    /** "сегодня", "завтра" or "через 5 дней". */
    fun countdownText(days: Int): String = when (days) {
        0 -> "сегодня"
        1 -> "завтра"
        else -> "через $days ${pluralDays(days)}"
    }
}
