package com.example.wishlist228.utils

import com.example.wishlist228.models.GiftFilter
import com.example.wishlist228.models.GiftListItem
import com.example.wishlist228.models.SortOrder

/**
 * Search, filtering and sorting of the gift list. These are plain functions without
 * Android classes, so they are covered by ordinary unit tests.
 */
object GiftQuery {

    /** Applies search and filters first, then the sort order. */
    fun apply(items: List<GiftListItem>, filter: GiftFilter, order: SortOrder): List<GiftListItem> =
        sort(items.filter { matches(it, filter) }, order)

    /**
     * The text search looks at the title, the description and the tags;
     * all filters must match at the same time (they work together).
     */
    fun matches(item: GiftListItem, filter: GiftFilter): Boolean {
        val query = filter.query.trim()
        val matchesQuery = query.isEmpty() ||
            item.gift.title.contains(query, ignoreCase = true) ||
            item.gift.description.contains(query, ignoreCase = true) ||
            item.tags.any { it.name.contains(query, ignoreCase = true) }

        return matchesQuery &&
            (filter.friendId == null || item.gift.friendId == filter.friendId) &&
            (filter.status == null || item.gift.status == filter.status) &&
            (filter.tagId == null || item.tags.any { it.id == filter.tagId })
    }

    fun sort(items: List<GiftListItem>, order: SortOrder): List<GiftListItem> =
        when (order) {
            SortOrder.NEWEST -> items.sortedByDescending { it.gift.createdAt }
            SortOrder.TITLE -> items.sortedBy { it.gift.title.lowercase() }
            SortOrder.PRICE_ASC -> items.sortedBy { it.gift.price }
            SortOrder.PRICE_DESC -> items.sortedByDescending { it.gift.price }
            SortOrder.RATING -> items.sortedWith(
                compareByDescending<GiftListItem> { it.gift.rating }.thenBy { it.gift.title.lowercase() }
            )
        }
}
