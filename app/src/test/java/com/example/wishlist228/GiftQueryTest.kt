package com.example.wishlist228

import com.example.wishlist228.TestData.gift
import com.example.wishlist228.TestData.item
import com.example.wishlist228.models.GiftFilter
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.models.SortOrder
import com.example.wishlist228.models.Tag
import com.example.wishlist228.utils.GiftQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GiftQueryTest {

    private val sport = Tag(id = 1, name = "спорт")
    private val home = Tag(id = 2, name = "дом")

    private val items = listOf(
        item(gift(1, friendId = 1, title = "Беговые кроссовки", price = 7500.0, rating = 5, createdAt = 10), "Максим", sport),
        item(gift(2, friendId = 1, title = "Настольная игра", price = 2500.0, rating = 4, status = GiftStatus.PURCHASED, createdAt = 30), "Максим"),
        item(gift(3, friendId = 2, title = "Тёплый плед", description = "Для дачи", price = 3000.0, rating = 4, status = GiftStatus.GIVEN, createdAt = 20), "Мама", home),
        item(gift(4, friendId = 2, title = "Сертификат в спа", price = 6000.0, rating = 5, createdAt = 40), "Мама")
    )

    private fun titles(list: List<com.example.wishlist228.models.GiftListItem>) = list.map { it.gift.id }

    @Test
    fun emptyFilter_keepsEverything() {
        assertEquals(4, GiftQuery.apply(items, GiftFilter(), SortOrder.NEWEST).size)
    }

    @Test
    fun search_ignoresCaseAndLooksInTitle() {
        val result = GiftQuery.apply(items, GiftFilter(query = "ПЛЕД"), SortOrder.NEWEST)
        assertEquals(listOf(3L), titles(result))
    }

    @Test
    fun search_looksInDescriptionAndTags() {
        assertEquals(listOf(3L), titles(GiftQuery.apply(items, GiftFilter(query = "дачи"), SortOrder.NEWEST)))
        assertEquals(listOf(1L), titles(GiftQuery.apply(items, GiftFilter(query = "спорт"), SortOrder.NEWEST)))
    }

    @Test
    fun search_ignoresSpacesAround() {
        assertEquals(listOf(3L), titles(GiftQuery.apply(items, GiftFilter(query = "  плед "), SortOrder.NEWEST)))
    }

    @Test
    fun filtersWorkTogether() {
        val filter = GiftFilter(friendId = 2, status = GiftStatus.IDEA)
        assertEquals(listOf(4L), titles(GiftQuery.apply(items, filter, SortOrder.NEWEST)))
    }

    @Test
    fun tagFilter_keepsOnlyGiftsWithThatTag() {
        val result = GiftQuery.apply(items, GiftFilter(tagId = home.id), SortOrder.NEWEST)
        assertEquals(listOf(3L), titles(result))
    }

    @Test
    fun nothingFound_returnsEmptyList() {
        assertTrue(GiftQuery.apply(items, GiftFilter(query = "самолёт"), SortOrder.NEWEST).isEmpty())
    }

    @Test
    fun sortNewest_putsLatestFirst() {
        assertEquals(listOf(4L, 2L, 3L, 1L), titles(GiftQuery.sort(items, SortOrder.NEWEST)))
    }

    @Test
    fun sortByTitle_isAlphabeticalIgnoringCase() {
        assertEquals(listOf(1L, 2L, 4L, 3L), titles(GiftQuery.sort(items, SortOrder.TITLE)))
    }

    @Test
    fun sortByPrice_worksBothWays() {
        assertEquals(listOf(2L, 3L, 4L, 1L), titles(GiftQuery.sort(items, SortOrder.PRICE_ASC)))
        assertEquals(listOf(1L, 4L, 3L, 2L), titles(GiftQuery.sort(items, SortOrder.PRICE_DESC)))
    }

    @Test
    fun sortByRating_bestFirstThenByTitle() {
        // rating 5: "Беговые кроссовки", "Сертификат в спа"; rating 4: "Настольная игра", "Тёплый плед"
        assertEquals(listOf(1L, 4L, 2L, 3L), titles(GiftQuery.sort(items, SortOrder.RATING)))
    }

    @Test
    fun filterIsActive_ignoresSortOrder() {
        assertFalse(GiftFilter(sortOverride = SortOrder.RATING).isActive)
        assertTrue(GiftFilter(query = "a").isActive)
        assertTrue(GiftFilter(status = GiftStatus.IDEA).isActive)
    }
}
