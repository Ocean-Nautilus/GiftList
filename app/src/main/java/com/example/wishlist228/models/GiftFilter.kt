package com.example.wishlist228.models

/** What the user typed in the search field and chose in the filters of the gift list. */
data class GiftFilter(
    val query: String = "",
    val friendId: Long? = null,
    val status: GiftStatus? = null,
    val tagId: Long? = null,
    /** A sort order chosen on the screen. `null` means "use the default from the settings". */
    val sortOverride: SortOrder? = null
) {
    /** `true` if the search text or at least one filter is set (the sort order does not count). */
    val isActive: Boolean
        get() = query.isNotBlank() || friendId != null || status != null || tagId != null
}

/** One row of the gift list: the gift with the name of the friend and its tags. */
data class GiftListItem(
    val gift: Gift,
    val friendName: String,
    val tags: List<Tag>
)
