package com.example.wishlist228.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.models.SortOrder
import com.example.wishlist228.ui.components.EmptyState
import com.example.wishlist228.ui.components.FilterChipMenu
import com.example.wishlist228.ui.components.GiftCard
import com.example.wishlist228.ui.components.LoadingBox
import com.example.wishlist228.viewmodels.GiftListUiState
import com.example.wishlist228.viewmodels.GiftListViewModel

/**
 * The main screen: all gifts with a search field, filters (friend, status, tag),
 * sorting, an "add" button and placeholders for the empty states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftListScreen(
    viewModel: GiftListViewModel,
    onAddGift: () -> Unit,
    onAddFriend: () -> Unit,
    onOpenGift: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подарки") },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Сортировка")
                        }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    leadingIcon = {
                                        if (order == state.sortOrder) {
                                            Icon(Icons.Filled.Check, contentDescription = null)
                                        }
                                    },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        sortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // A gift always belongs to a friend, so there is nothing to add before the first friend
            if (!state.isLoading && state.friends.isNotEmpty()) {
                FloatingActionButton(onClick = onAddGift) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить подарок")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> LoadingBox()

                state.friends.isEmpty() -> EmptyState(
                    icon = Icons.Filled.People,
                    title = "Сначала добавьте друга",
                    message = "Подарки привязаны к людям: добавьте того, для кого выбираете подарок.",
                    actionLabel = "Добавить друга",
                    onAction = onAddFriend
                )

                state.totalGifts == 0 -> EmptyState(
                    icon = Icons.Filled.CardGiftcard,
                    title = "Пока нет ни одного подарка",
                    message = "Добавьте первую идею, и она появится здесь.",
                    actionLabel = "Добавить подарок",
                    onAction = onAddGift
                )

                else -> GiftListContent(state = state, viewModel = viewModel, onOpenGift = onOpenGift)
            }
        }
    }
}

@Composable
private fun GiftListContent(
    state: GiftListUiState,
    viewModel: GiftListViewModel,
    onOpenGift: (Long) -> Unit
) {
    val filter = state.filter

    Column(modifier = Modifier.fillMaxSize()) {
        // ----- Search -----
        OutlinedTextField(
            value = filter.query,
            onValueChange = { viewModel.setQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
            singleLine = true,
            placeholder = { Text("Поиск по названию, описанию и тегам") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (filter.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Очистить поиск")
                    }
                }
            }
        )

        // ----- Filters: they work together -----
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChipMenu(
                title = "Друг",
                selectedLabel = state.friends.firstOrNull { it.id == filter.friendId }?.name,
                options = state.friends.map { it.id to it.name },
                onSelect = { viewModel.setFriendFilter(it) }
            )
            FilterChipMenu(
                title = "Статус",
                selectedLabel = filter.status?.label,
                options = GiftStatus.entries.map { it to it.label },
                onSelect = { viewModel.setStatusFilter(it) }
            )
            FilterChipMenu(
                title = "Тег",
                selectedLabel = state.tags.firstOrNull { it.id == filter.tagId }?.name,
                options = state.tags.map { it.id to it.name },
                onSelect = { viewModel.setTagFilter(it) }
            )
            if (filter.isActive) {
                TextButton(onClick = { viewModel.clearFilters() }) { Text("Сбросить") }
            }
        }

        // ----- The list -----
        if (state.items.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Search,
                title = "Ничего не найдено",
                message = "Измените запрос или сбросьте фильтры.",
                actionLabel = "Сбросить фильтры",
                onAction = { viewModel.clearFilters() }
            )
        } else {
            Text(
                text = "Найдено: ${state.items.size} из ${state.totalGifts}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // The bottom padding keeps the last card above the floating button
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = state.items, key = { it.gift.id }) { item ->
                    GiftCard(
                        item = item,
                        currencySymbol = state.currencySymbol,
                        onClick = { onOpenGift(item.gift.id) }
                    )
                }
            }
        }
    }
}
