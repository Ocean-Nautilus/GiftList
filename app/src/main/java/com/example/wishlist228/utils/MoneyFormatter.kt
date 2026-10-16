package com.example.wishlist228.utils

import java.util.Locale

/** Formats amounts of money for the UI, e.g. "15 990 ₽". */
object MoneyFormatter {

    private val russian = Locale("ru")

    fun format(amount: Double, currencySymbol: String): String {
        val rounded = Math.round(amount)
        val number = if (amount == rounded.toDouble()) {
            String.format(russian, "%,d", rounded)
        } else {
            String.format(russian, "%,.2f", amount)
        }
        return "$number $currencySymbol"
    }
}
