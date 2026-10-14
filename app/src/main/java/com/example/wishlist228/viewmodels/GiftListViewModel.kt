package com.example.wishlist228.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlist228.GiftListApplication
import com.example.wishlist228.models.Friend
import com.example.wishlist228.models.Gift
import com.example.wishlist228.models.GiftFilter
import com.example.wishlist228.models.GiftListItem
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.models.GiftWithTags
import com.example.wishlist228.models.SortOrder
import com.example.wishlist228.models.Tag
import com.example.wishlist228.utils.GiftQuery
import com.example.wishlist228.utils.safeDbCall
import com.example.wishlist228.utils.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GiftListUiState(
    val isLoading: Boolean = true,
    /** Gifts after search, filters and sorting. */
    val items: List<GiftListItem> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val filter: GiftFilter = GiftFilter(),
    val sortOrder: SortOrder = SortOrder.NEWEST,
    /** Number of gifts before filtering: tells "no gifts yet" from "nothing found". */
    val totalGifts: Int = 0,
    val currencySymbol: String = "₽"
)

/** State of the detail screen. */
sealed interface GiftDetailState {
    data object Loading : GiftDetailState

    /** [gift] is `null` when the gift does not exist (for example, it was just deleted). */
    data class Loaded(val gift: GiftWithTags?) : GiftDetailState
}

/**
 * Logic of the gift list, the detail screen and the add/edit form (MVVM).
 * It does not know anything about Compose: the screens only observe [uiState].
 */
class GiftListViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as GiftListApplication).container
    private val repository = container.repository

    private val filterState = MutableStateFlow(GiftFilter())

    // One-time messages for a Snackbar (errors, confirmations)
    private val eventChannel = Channel<String>(Channel.BUFFERED)
    val events: Flow<String> = eventChannel.receiveAsFlow()

    val uiState: StateFlow<GiftListUiState> = combine(
        repository.gifts,
        repository.friends,
        repository.tags,
        container.settingsStore.settings,
        filterState
    ) { gifts, friends, tags, settings, filter ->
        val friendNames = friends.associate { it.id to it.name }
        val order = filter.sortOverride ?: settings.defaultSort

        val items = GiftQuery.apply(
            items = gifts.map { GiftListItem(it.gift, friendNames[it.gift.friendId].orEmpty(), it.tags) },
            filter = filter,
            order = order
        )

        GiftListUiState(
            isLoading = false,
            items = items,
            friends = friends,
            tags = tags,
            filter = filter,
            sortOrder = order,
            totalGifts = gifts.size,
            currencySymbol = settings.currencySymbol
        )
    }.catch {
        eventChannel.trySend("Не удалось загрузить подарки: ${it.toUserMessage()}")
        emit(GiftListUiState(isLoading = false))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GiftListUiState())

    // ---------- Search, filters, sorting ----------

    fun setQuery(query: String) = filterState.update { it.copy(query = query) }

    fun setFriendFilter(friendId: Long?) = filterState.update { it.copy(friendId = friendId) }

    fun setStatusFilter(status: GiftStatus?) = filterState.update { it.copy(status = status) }

    fun setTagFilter(tagId: Long?) = filterState.update { it.copy(tagId = tagId) }

    fun setSortOrder(order: SortOrder) = filterState.update { it.copy(sortOverride = order) }

    /** Clears the search text and all filters, the chosen sort order stays. */
    fun clearFilters() = filterState.update { GiftFilter(sortOverride = it.sortOverride) }

    // ---------- Messages ----------

    /** Shows a short message in the Snackbar of the main screen. */
    fun showMessage(text: String) {
        eventChannel.trySend(text)
    }

    // ---------- Single gift ----------

    fun observeGiftDetail(id: Long): Flow<GiftDetailState> =
        repository.observeGift(id).map { GiftDetailState.Loaded(it) }

    /** Loads a gift for the edit form. Returns `null` if it does not exist or reading failed. */
    suspend fun loadGift(id: Long): GiftWithTags? =
        safeDbCall { repository.getGift(id) }.getOrElse {
            eventChannel.trySend("Не удалось открыть подарок: ${it.toUserMessage()}")
            null
        }

    /** Creates or updates a gift. Returns `true` on success, otherwise shows an error message. */
    suspend fun saveGift(gift: Gift, tagNames: List<String>): Boolean {
        val result = safeDbCall { repository.saveGift(gift, tagNames) }
        result.exceptionOrNull()?.let {
            eventChannel.trySend("Не удалось сохранить подарок: ${it.toUserMessage()}")
        }
        return result.isSuccess
    }

    /** Deletes a gift. Returns `true` on success. */
    suspend fun deleteGift(id: Long): Boolean {
        val result = safeDbCall { repository.deleteGift(id) }
        result.exceptionOrNull()?.let {
            eventChannel.trySend("Не удалось удалить подарок: ${it.toUserMessage()}")
        }
        return result.isSuccess
    }

    fun changeStatus(giftId: Long, status: GiftStatus) {
        viewModelScope.launch {
            val result = safeDbCall { repository.updateGiftStatus(giftId, status) }
            result.exceptionOrNull()?.let {
                eventChannel.trySend("Не удалось изменить статус: ${it.toUserMessage()}")
            }
        }
    }
}
