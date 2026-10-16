package com.example.wishlist228

import com.example.wishlist228.utils.FormValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidatorTest {

    @Test
    fun parseMoney_acceptsCommaAndSpaces() {
        assertEquals(1500.5, FormValidator.parseMoney("1 500,50")!!, 0.0001)
        assertEquals(990.0, FormValidator.parseMoney("990")!!, 0.0001)
    }

    @Test
    fun parseMoney_emptyTextMeansZero() {
        assertEquals(0.0, FormValidator.parseMoney("   ")!!, 0.0001)
    }

    @Test
    fun parseMoney_rejectsWrongValues() {
        assertNull(FormValidator.parseMoney("abc"))
        assertNull(FormValidator.parseMoney("-5"))
        assertNull(FormValidator.parseMoney("NaN"))
        assertNull(FormValidator.parseMoney("Infinity"))
        assertNull(FormValidator.parseMoney("1000000000"))
    }

    @Test
    fun isHttpUrl_requiresSchemeAndHost() {
        assertTrue(FormValidator.isHttpUrl("https://example.com/item"))
        assertTrue(FormValidator.isHttpUrl("HTTP://example.com"))
        assertFalse(FormValidator.isHttpUrl("example.com"))
        assertFalse(FormValidator.isHttpUrl("https://"))
        assertFalse(FormValidator.isHttpUrl("ftp://example.com"))
    }

    @Test
    fun validateGift_marksEveryWrongField() {
        val errors = FormValidator.validateGift(
            title = " ",
            priceText = "abc",
            friendId = null,
            link = "not a link",
            imageUrl = "also not"
        )
        assertNotNull(errors.title)
        assertNotNull(errors.price)
        assertNotNull(errors.friend)
        assertNotNull(errors.link)
        assertNotNull(errors.imageUrl)
        assertTrue(errors.hasErrors)
    }

    @Test
    fun validateGift_acceptsCorrectForm() {
        val errors = FormValidator.validateGift(
            title = "Наушники",
            priceText = "4 500",
            friendId = 1L,
            link = "",
            imageUrl = "https://example.com/a.jpg"
        )
        assertFalse(errors.hasErrors)
    }

    @Test
    fun validateGift_rejectsTooLongTitle() {
        val errors = FormValidator.validateGift("a".repeat(101), "0", 1L, "", "")
        assertNotNull(errors.title)
    }

    @Test
    fun validateFriend_requiresNameAndValidBudget() {
        assertNotNull(FormValidator.validateFriend("", "100").name)
        assertNotNull(FormValidator.validateFriend("Анна", "-1").budget)
        assertFalse(FormValidator.validateFriend("Анна", "").hasErrors)
    }

    @Test
    fun parseTags_removesDuplicatesAndEmptyItems() {
        val tags = FormValidator.parseTags("книги, Спорт; дом, КНИГИ,, ")
        assertEquals(listOf("книги", "Спорт", "дом"), tags)
    }

    @Test
    fun parseTags_limitsTheNumberOfTags() {
        val text = (1..20).joinToString(",") { "tag$it" }
        assertEquals(FormValidator.MAX_TAGS, FormValidator.parseTags(text).size)
    }
}
