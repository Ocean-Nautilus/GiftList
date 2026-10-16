package com.example.wishlist228.database

import com.example.wishlist228.models.Friend
import com.example.wishlist228.models.Gift
import com.example.wishlist228.models.GiftStatus
import java.time.LocalDate

/**
 * Fills an empty app with sample friends and gifts. Birthdays are placed relative to
 * today, so that the reminders and the statistics can be shown right away.
 */
class DemoData(private val repository: GiftRepository) {

    suspend fun insert() {
        val today = LocalDate.now()

        val anna = repository.saveFriend(
            Friend(
                name = "Анна",
                birthdayEpochDay = today.plusDays(5).withYear(1996).toEpochDay(),
                budget = 6000.0,
                notes = "Любит технику и книги"
            )
        )
        val maxim = repository.saveFriend(
            Friend(
                name = "Максим",
                birthdayEpochDay = today.plusDays(21).withYear(1993).toEpochDay(),
                budget = 9000.0,
                notes = "Увлекается бегом и настолками"
            )
        )
        val mom = repository.saveFriend(
            Friend(
                name = "Мама",
                birthdayEpochDay = today.plusDays(60).withYear(1968).toEpochDay(),
                budget = 12000.0
            )
        )

        saveGift(anna, "Беспроводные наушники", 4500.0, 5, GiftStatus.PURCHASED, "техника", "день рождения")
        saveGift(anna, "Набор ароматических свечей", 1200.0, 3, GiftStatus.IDEA, "дом")
        saveGift(anna, "Книга «Мастер и Маргарита»", 700.0, 4, GiftStatus.IDEA, "книги")

        saveGift(maxim, "Беговые кроссовки", 7500.0, 5, GiftStatus.IDEA, "спорт", "день рождения")
        saveGift(maxim, "Настольная игра «Каркассон»", 2500.0, 4, GiftStatus.PURCHASED, "игры")

        saveGift(mom, "Тёплый плед", 3000.0, 4, GiftStatus.GIVEN, "дом")
        saveGift(mom, "Сертификат в спа", 6000.0, 5, GiftStatus.IDEA, "впечатления", "день рождения")
    }

    private suspend fun saveGift(
        friendId: Long,
        title: String,
        price: Double,
        rating: Int,
        status: GiftStatus,
        vararg tags: String
    ) {
        repository.saveGift(
            Gift(friendId = friendId, title = title, price = price, rating = rating, status = status),
            tags.toList()
        )
    }
}
