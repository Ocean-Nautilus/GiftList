package com.example.wishlist228

import com.example.wishlist228.utils.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatterTest {

    // The thousands separator depends on the JDK/ICU version, so only digits are compared
    private fun digits(text: String) = text.filter { it.isDigit() }

    @Test
    fun wholeAmount_hasNoFraction() {
        val text = MoneyFormatter.format(15990.0, "₽")
        assertEquals("15990", digits(text))
        assertTrue(text.endsWith(" ₽"))
    }

    @Test
    fun fractionalAmount_showsTwoDecimals() {
        val text = MoneyFormatter.format(10.5, "$")
        assertTrue(text.startsWith("10,50"))
        assertTrue(text.endsWith(" $"))
    }

    @Test
    fun zero_isFormatted() {
        assertEquals("0 €", MoneyFormatter.format(0.0, "€"))
    }
}
