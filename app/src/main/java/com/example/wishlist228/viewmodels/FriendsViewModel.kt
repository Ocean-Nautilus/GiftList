package com.example.wishlist228.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlist228.GiftListApplication
import com.example.wishlist228.models.Friend
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.utils.DateUtils
import com.example.wishlist228.utils.safeDbCall
import com.example.wishlist228.utils.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** A friend with the numbers shown on his card. */
data class FriendUi(
    val friend: Friend,
    val daysUntilBirthday: Int,
    val turningAge: Int,
    val giftsCount: Int,
    /** Money spent on gifts that are already bought or given. */
    val spent: Double
)

data class FriendsUiState(
    val isLoading: Boolean = true,
    /** Sorted by the nearest birthday. */
    val friends: List<FriendUi> = emptyList(),
    val currencySymbol: String = "₽"
)

/** Logic of the "Friends" screen: the list of people and their CRUD. */
class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as GiftListApplication).container
    private val repository = container.repository

    private val eventChannel = Channel<String>(Channel.BUFFERED)
    val events: Flow<String> = eventChannel.receiveAsFlow()

    val uiState: StateFlow<FriendsUiState> = combine(
        repository.friends,
        repository.gifts,
        container.settingsStore.settings
    ) { friends, gifts, settings ->
        val today = LocalDate.now()
        val items = friends.map { friend ->
            val friendGifts = gifts.filter { it.gift.friendId == friend.id }
            FriendUi(
                friend = friend,
                daysUntilBirthday = DateUtils.daysUntilBirthday(friend.birthdayEpochDay, today),
                turningAge = DateUtils.turningAge(friend.birthdayEpochDay, today),
                giftsCount = friendGifts.size,
                spent = friendGifts
                    .filter { it.gift.status != GiftStatus.IDEA }
                    .sumOf { it.gift.price }
            )
        }.sortedBy { it.daysUntilBirthday }

        FriendsUiState(isLoading = false, friends = items, currencySymbol = settings.currencySymbol)
    }.catch {
        eventChannel.trySend("Не удалось загрузить друзей: ${it.toUserMessage()}")
        emit(FriendsUiState(isLoading = false))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FriendsUiState())

    /** Creates (id == 0) or updates a friend. Returns `true` on success. */
    suspend fun saveFriend(friend: Friend): Boolean {
        val result = safeDbCall { repository.saveFriend(friend) }
        result.exceptionOrNull()?.let {
            eventChannel.trySend("Не удалось сохранить друга: ${it.toUserMessage()}")
        }
        return result.isSuccess
    }

    /** Deletes the friend together with all his gifts. Returns `true` on success. */
    suspend fun deleteFriend(friend: Friend): Boolean {
        val result = safeDbCall { repository.deleteFriend(friend) }
        result.exceptionOrNull()?.let {
            eventChannel.trySend("Не удалось удалить друга: ${it.toUserMessage()}")
        }
        return result.isSuccess
    }
}
