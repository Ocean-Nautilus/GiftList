package com.example.wishlist228

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WishlistUiState(
    val categories: List<WishCategory> = listOf(
        WishCategory("1", "Мой день рождения"),
        WishCategory("2", "Идеи для дома"),
        WishCategory("3", "Техника")
    ),
    val selectedCategory: WishCategory? = WishCategory("1", "Мой день рождения"),
    val wishes: List<WishItem> = listOf(
        WishItem(
            title = "Беспроводные наушники",
            price = "15 990 ₽",
            link = "https://example.com/item",
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
            category = "Мой день рождения",
            priority = WishPriority.HIGH
        ),
        WishItem(
            title = "Увлажнитель воздуха",
            price = "4 500 ₽",
            imageUrl = "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=500",
            category = "Идеи для дома",
            priority = WishPriority.MEDIUM
        ),
        WishItem(
            title = "Настольная игра «Крылья»",
            price = "3 200 ₽",
            category = "Мой день рождения",
            priority = WishPriority.MEDIUM,
            isReserved = true,
            reservedByName = "Алина"
        )
    ),
    val friends: List<Friend> = listOf(
        Friend("1", "Анна Смирнова", "https://i.pravatar.cc/150?img=1", "12 Октября", 11, 8),
        Friend("2", "Михаил Петров", "https://i.pravatar.cc/150?img=3", "28 Октября", 27, 14),
        Friend("3", "Елена Морозова", "https://i.pravatar.cc/150?img=5", "5 Ноября", 35, 5)
    ),
    val collections: List<IdeaCollection> = listOf(
        IdeaCollection("1", "Топ-10 гаджетов 2026", "Идеи для гиков", "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=500"),
        IdeaCollection("2", "Уютный дом", "Товары для комфорта", "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=500"),
        IdeaCollection("3", "Для любителей кофе", "От зерен до кофемашин", "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=500")
    )
)

class WishlistViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    fun selectCategory(category: WishCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun addWish(title: String, price: String?, link: String?, priority: WishPriority) {
        val currentCategory = _uiState.value.selectedCategory?.name ?: "Основной"
        val newWish = WishItem(
            title = title,
            price = if (price.isNullOrBlank()) null else price,
            link = if (link.isNullOrBlank()) null else link,
            category = currentCategory,
            priority = priority
        )
        _uiState.update { state ->
            state.copy(wishes = listOf(newWish) + state.wishes)
        }
    }

    fun toggleReservation(wishId: String) {
        _uiState.update { state ->
            state.copy(
                wishes = state.wishes.map { wish ->
                    if (wish.id == wishId) {
                        wish.copy(
                            isReserved = !wish.isReserved,
                            reservedByName = if (!wish.isReserved) "Вы" else null
                        )
                    } else wish
                }
            )
        }
    }
}