package com.example.wishlist228.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wishlist228.models.Gift
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.ui.components.DropdownField
import com.example.wishlist228.ui.components.EmptyState
import com.example.wishlist228.ui.components.LoadingBox
import com.example.wishlist228.ui.components.RatingBar
import com.example.wishlist228.utils.FormValidator
import com.example.wishlist228.utils.GiftFormErrors
import com.example.wishlist228.utils.appendTag
import com.example.wishlist228.utils.toInputText
import com.example.wishlist228.viewmodels.GiftListViewModel
import kotlinx.coroutines.launch

/**
 * The add / edit form of a gift. [giftId] == 0 creates a new gift, otherwise the gift is edited.
 * Everything typed by the user is checked before saving: wrong fields are marked red
 * and a message is shown, a failed save never closes the form or crashes the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftFormScreen(
    viewModel: GiftListViewModel,
    giftId: Long,
    presetFriendId: Long?,
    onClose: () -> Unit,
    onAddFriend: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val isEdit = giftId != 0L

    // rememberSaveable: what the user has typed survives screen rotation
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var link by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var tagsText by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableStateOf(3) }
    var status by rememberSaveable { mutableStateOf(GiftStatus.IDEA) }
    var friendId by rememberSaveable { mutableStateOf(presetFriendId) }
    var createdAt by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var isLoaded by rememberSaveable { mutableStateOf(!isEdit) }

    var isSaving by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(GiftFormErrors()) }

    // Editing: fill the fields with the saved gift (once)
    LaunchedEffect(giftId) {
        if (isEdit && !isLoaded) {
            val saved = viewModel.loadGift(giftId)
            if (saved == null) {
                onClose()
            } else {
                val gift = saved.gift
                title = gift.title
                description = gift.description
                priceText = gift.price.toInputText()
                link = gift.link
                imageUrl = gift.imageUrl
                tagsText = saved.tags.joinToString(", ") { it.name }
                rating = gift.rating
                status = gift.status
                friendId = gift.friendId
                createdAt = gift.createdAt
                isLoaded = true
            }
        }
    }

    // A new gift: choose the first friend so that the user has one tap less
    LaunchedEffect(state.friends, isLoaded) {
        if (isLoaded && friendId == null && state.friends.isNotEmpty()) {
            friendId = state.friends.first().id
        }
    }

    fun save() {
        val validation = FormValidator.validateGift(title, priceText, friendId, link, imageUrl)
        errors = validation
        if (validation.hasErrors) {
            viewModel.showMessage("Проверьте поля, отмеченные красным")
            return
        }
        val chosenFriend = friendId ?: return
        val gift = Gift(
            id = giftId,
            friendId = chosenFriend,
            title = title.trim(),
            description = description.trim(),
            price = FormValidator.parseMoney(priceText) ?: 0.0,
            link = link.trim(),
            imageUrl = imageUrl.trim(),
            rating = rating,
            status = status,
            createdAt = createdAt
        )
        scope.launch {
            isSaving = true
            val saved = viewModel.saveGift(gift, FormValidator.parseTags(tagsText))
            isSaving = false
            // On an error the ViewModel has shown a message and the form stays open
            if (saved) onClose()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Редактирование подарка" else "Новый подарок") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Закрыть без сохранения")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading || !isLoaded -> LoadingBox(Modifier.padding(padding))

            state.friends.isEmpty() -> EmptyState(
                icon = Icons.Filled.People,
                title = "Нужен хотя бы один друг",
                message = "Подарок создаётся для конкретного человека.",
                modifier = Modifier.padding(padding),
                actionLabel = "Добавить друга",
                onAction = onAddFriend
            )

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DropdownField(
                    label = "Для кого *",
                    selectedText = state.friends.firstOrNull { it.id == friendId }?.name.orEmpty(),
                    options = state.friends,
                    optionText = { it.name },
                    onSelect = {
                        friendId = it.id
                        errors = errors.copy(friend = null)
                    },
                    errorText = errors.friend
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errors = errors.copy(title = null)
                    },
                    label = { Text("Название *") },
                    singleLine = true,
                    isError = errors.title != null,
                    supportingText = { if (errors.title != null) Text(errors.title.orEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        errors = errors.copy(price = null)
                    },
                    label = { Text("Цена, ${state.currencySymbol}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errors.price != null,
                    supportingText = { if (errors.price != null) Text(errors.price.orEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Рейтинг: насколько хочется подарить",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
                RatingBar(rating = rating, starSize = 40.dp, onRatingChange = { rating = it })

                Text(
                    text = "Статус",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GiftStatus.entries.forEach { option ->
                        FilterChip(
                            selected = status == option,
                            onClick = { status = option },
                            label = { Text(option.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = link,
                    onValueChange = {
                        link = it
                        errors = errors.copy(link = null)
                    },
                    label = { Text("Ссылка на магазин") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    isError = errors.link != null,
                    supportingText = { if (errors.link != null) Text(errors.link.orEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = {
                        imageUrl = it
                        errors = errors.copy(imageUrl = null)
                    },
                    label = { Text("Ссылка на фото") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    isError = errors.imageUrl != null,
                    supportingText = { if (errors.imageUrl != null) Text(errors.imageUrl.orEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Теги через запятую") },
                    placeholder = { Text("день рождения, книги") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.tags.isNotEmpty()) {
                    // Tags used before: one tap adds the tag to the field
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { tagsText = appendTag(tagsText, tag.name) },
                                label = { Text("#${tag.name}") }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    ) { Text("Отмена") }
                    Button(
                        onClick = { save() },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Сохранить")
                        }
                    }
                }
            }
        }
    }
}
