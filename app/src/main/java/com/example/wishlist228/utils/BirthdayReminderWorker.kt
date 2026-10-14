package com.example.wishlist228.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wishlist228.GiftListApplication
import com.example.wishlist228.models.Friend
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.models.GiftWithTags
import kotlinx.coroutines.flow.first

/**
 * The "feature" of the app: once a day it looks for friends whose birthday is close and
 * reminds the user about the gift: nothing chosen yet, only ideas (the best-rated idea
 * is suggested) or the gift is already bought. Also reports how much of the budget is left.
 */
class BirthdayReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val container = (applicationContext as GiftListApplication).container
            val settings = container.settingsStore.settings.first()
            val force = inputData.getBoolean(KEY_FORCE, false)
            if (!settings.remindersEnabled && !force) return Result.success()

            val friends = container.repository.friends.first()
            val gifts = container.repository.gifts.first()
            val currency = settings.currencySymbol

            val upcoming = friends
                .map { friend -> friend to DateUtils.daysUntilBirthday(friend.birthdayEpochDay) }
                .filter { (_, days) -> days <= settings.remindDaysBefore }
                .sortedBy { (_, days) -> days }

            if (upcoming.isEmpty()) {
                if (force) {
                    NotificationHelper.show(
                        applicationContext,
                        TEST_NOTIFICATION_ID,
                        "Напоминания работают",
                        "В ближайшие ${settings.remindDaysBefore} ${DateUtils.pluralDays(settings.remindDaysBefore)} " +
                            "дней рождения нет. Когда они появятся, вы получите уведомление."
                    )
                }
                return Result.success()
            }

            upcoming.forEach { (friend, days) ->
                val friendGifts = gifts.filter { it.gift.friendId == friend.id }
                NotificationHelper.show(
                    applicationContext,
                    NOTIFICATION_ID_BASE + friend.id.toInt(),
                    title = titleFor(friend, days),
                    text = textFor(friend, friendGifts, currency)
                )
            }
            Result.success()
        } catch (e: Exception) {
            // A reminder is not critical: do not crash, try again at the next run
            Result.retry()
        }
    }

    private fun titleFor(friend: Friend, days: Int): String =
        if (days == 0) {
            "Сегодня день рождения: ${friend.name}"
        } else {
            "День рождения ${DateUtils.countdownText(days)}: ${friend.name}"
        }

    private fun textFor(friend: Friend, friendGifts: List<GiftWithTags>, currency: String): String {
        val giftLine = when {
            friendGifts.isEmpty() ->
                "Подарок ещё не выбран, самое время добавить идею."
            friendGifts.none { it.gift.status != GiftStatus.IDEA } -> {
                val best = friendGifts.maxByOrNull { it.gift.rating }?.gift
                "Подарок ещё не куплен. Лучшая идея: «${best?.title}» (${best?.rating} из 5)."
            }
            else -> "Подарок уже куплен, осталось вручить."
        }
        if (friend.budget <= 0) return giftLine

        val spent = friendGifts.filter { it.gift.status != GiftStatus.IDEA }.sumOf { it.gift.price }
        val budgetLine = "Потрачено ${MoneyFormatter.format(spent, currency)} " +
            "из ${MoneyFormatter.format(friend.budget, currency)}."
        return "$giftLine $budgetLine"
    }

    companion object {
        const val KEY_FORCE = "force"
        private const val TEST_NOTIFICATION_ID = 1
        private const val NOTIFICATION_ID_BASE = 1000
    }
}
