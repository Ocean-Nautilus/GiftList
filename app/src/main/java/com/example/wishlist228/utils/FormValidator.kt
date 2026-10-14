package com.example.wishlist228.utils

/** Error texts for the gift form. A `null` field means "no error". */
data class GiftFormErrors(
    val title: String? = null,
    val price: String? = null,
    val friend: String? = null,
    val link: String? = null,
    val imageUrl: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(title, price, friend, link, imageUrl).any { it != null }
}

/** Error texts for the friend form. */
data class FriendFormErrors(
    val name: String? = null,
    val budget: String? = null
) {
    val hasErrors: Boolean
        get() = name != null || budget != null
}

/** Checks the text typed by the user before it is saved to the database. */
object FormValidator {

    const val MAX_TITLE_LENGTH = 100
    const val MAX_NAME_LENGTH = 50
    const val MAX_TAG_LENGTH = 30
    const val MAX_TAGS = 10
    const val MAX_MONEY = 100_000_000.0

    /**
     * Converts text such as "1 500,50" to a number.
     * An empty text means 0. Returns `null` if the text is not a valid non-negative amount.
     */
    fun parseMoney(text: String): Double? {
        val normalized = text.trim().replace(" ", "").replace(',', '.')
        if (normalized.isEmpty()) return 0.0
        val value = normalized.toDoubleOrNull() ?: return null
        if (value.isNaN() || value.isInfinite() || value < 0 || value > MAX_MONEY) return null
        return value
    }

    fun isHttpUrl(text: String): Boolean {
        val lower = text.trim().lowercase()
        return (lower.startsWith("http://") || lower.startsWith("https://")) && lower.length > 8
    }

    fun validateGift(
        title: String,
        priceText: String,
        friendId: Long?,
        link: String,
        imageUrl: String
    ): GiftFormErrors = GiftFormErrors(
        title = when {
            title.isBlank() -> "Введите название подарка"
            title.trim().length > MAX_TITLE_LENGTH -> "Не длиннее $MAX_TITLE_LENGTH символов"
            else -> null
        },
        price = if (parseMoney(priceText) == null) "Введите корректную цену, например 1500" else null,
        friend = if (friendId == null) "Выберите, для кого подарок" else null,
        link = if (link.isNotBlank() && !isHttpUrl(link)) "Ссылка должна начинаться с http:// или https://" else null,
        imageUrl = if (imageUrl.isNotBlank() && !isHttpUrl(imageUrl)) "Ссылка должна начинаться с http:// или https://" else null
    )

    fun validateFriend(name: String, budgetText: String): FriendFormErrors = FriendFormErrors(
        name = when {
            name.isBlank() -> "Введите имя"
            name.trim().length > MAX_NAME_LENGTH -> "Не длиннее $MAX_NAME_LENGTH символов"
            else -> null
        },
        budget = if (parseMoney(budgetText) == null) "Введите корректную сумму, например 5000" else null
    )

    /** Splits "книги, спорт; дом" into unique tags (no case duplicates, limited count and length). */
    fun parseTags(text: String): List<String> =
        text.split(',', ';')
            .map { it.trim().take(MAX_TAG_LENGTH) }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(MAX_TAGS)
}
