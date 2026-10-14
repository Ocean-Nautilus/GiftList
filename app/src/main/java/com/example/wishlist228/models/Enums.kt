package com.example.wishlist228.models

/** Lifecycle of a gift: from an idea to a gift that was already handed over. */
enum class GiftStatus(val label: String) {
    IDEA("Идея"),
    PURCHASED("Куплен"),
    GIVEN("Подарен")
}

/** Sort orders available for the gift list. */
enum class SortOrder(val label: String) {
    NEWEST("Сначала новые"),
    TITLE("По названию"),
    PRICE_ASC("Сначала дешёвые"),
    PRICE_DESC("Сначала дорогие"),
    RATING("По рейтингу")
}

/** App theme choice stored in the settings. */
enum class ThemeMode(val label: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая"),
    DARK("Тёмная")
}
