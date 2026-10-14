package com.example.wishlist228.models

/** User preferences, persisted with DataStore. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultSort: SortOrder = SortOrder.NEWEST,
    val currencySymbol: String = "₽",
    val remindersEnabled: Boolean = false,
    /** How many days before a birthday the reminder is shown. */
    val remindDaysBefore: Int = 7
) {
    companion object {
        val CURRENCIES = listOf("₽", "$", "€")
        val REMIND_DAYS_OPTIONS = listOf(1, 3, 7, 14)
    }
}
