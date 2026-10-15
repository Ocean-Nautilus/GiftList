package com.example.wishlist228.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wishlist228.models.Friend
import com.example.wishlist228.ui.components.BirthdayPickerDialog
import com.example.wishlist228.ui.components.ConfirmDialog
import com.example.wishlist228.ui.components.EmptyState
import com.example.wishlist228.ui.components.LoadingBox
import com.example.wishlist228.utils.DateUtils
import com.example.wishlist228.utils.FormValidator
import com.example.wishlist228.utils.FriendFormErrors
import com.example.wishlist228.utils.MoneyFormatter
import com.example.wishlist228.utils.toInputText
import com.example.wishlist228.viewmodels.FriendUi
import com.example.wishlist228.viewmodels.FriendsViewModel
import kotlinx.coroutines.launch

/**
 * The list of friends sorted by the nearest birthday, with the countdown to the holiday,
 * the number of gifts and the used part of the budget. Friends can be added, edited and deleted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(viewModel: FriendsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // The dialog is open when `showDialog` is true; `editedFriend == null` means "add a new one"
    var showDialog by remember { mutableStateOf(false) }
    var editedFriend by remember { mutableStateOf<Friend?>(null) }
    var friendToDelete by remember { mutableStateOf<FriendUi?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Друзья") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editedFriend = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить друга")
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
                    title = "Список друзей пуст",
                    message = "Добавьте первого друга и дату его рождения: приложение напомнит, когда пора выбирать подарок.",
                    actionLabel = "Добавить друга",
                    onAction = {
                        editedFriend = null
                        showDialog = true
                    }
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = state.friends, key = { it.friend.id }) { item ->
                        FriendCard(
                            item = item,
                            currencySymbol = state.currencySymbol,
                            onEdit = {
                                editedFriend = item.friend
                                showDialog = true
                            },
                            onDelete = { friendToDelete = item }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        FriendDialog(
            initial = editedFriend,
            currencySymbol = state.currencySymbol,
            onSave = { friend -> viewModel.saveFriend(friend) },
            onDismiss = { showDialog = false }
        )
    }

    friendToDelete?.let { item ->
        ConfirmDialog(
            title = "Удалить друга?",
            text = if (item.giftsCount > 0) {
                "Вы уверены, что хотите удалить «${item.friend.name}»? " +
                    "Вместе с ним будут удалены все его подарки (${item.giftsCount})."
            } else {
                "Вы уверены, что хотите удалить «${item.friend.name}»?"
            },
            confirmText = "Удалить",
            onConfirm = {
                friendToDelete = null
                scope.launch { viewModel.deleteFriend(item.friend) }
            },
            onDismiss = { friendToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendCard(
    item: FriendUi,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val friend = item.friend
    // The birthday is close: highlight the countdown
    val isSoon = item.daysUntilBirthday <= 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = friend.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = friend.name, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Cake,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSoon) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${DateUtils.formatDayMonth(friend.birthdayEpochDay)}, " +
                                "исполнится ${item.turningAge}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = DateUtils.countdownText(item.daysUntilBirthday),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSoon) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редактировать друга")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить друга",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "Подарков: ${item.giftsCount}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (friend.budget > 0) {
                val progress = (item.spent / friend.budget).toFloat().coerceIn(0f, 1f)
                val overBudget = item.spent > friend.budget
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (overBudget) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Потрачено ${MoneyFormatter.format(item.spent, currencySymbol)} " +
                        "из ${MoneyFormatter.format(friend.budget, currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (overBudget) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (friend.notes.isNotBlank()) {
                Text(
                    text = friend.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** The dialog for adding a new friend ([initial] == null) or editing an existing one. */
@Composable
private fun FriendDialog(
    initial: Friend?,
    currencySymbol: String,
    onSave: suspend (Friend) -> Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var birthday by rememberSaveable { mutableStateOf(initial?.birthdayEpochDay) }
    var budgetText by rememberSaveable { mutableStateOf(initial?.budget?.toInputText().orEmpty()) }
    var notes by rememberSaveable { mutableStateOf(initial?.notes.orEmpty()) }

    var errors by remember { mutableStateOf(FriendFormErrors()) }
    var birthdayError by remember { mutableStateOf<String?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    fun save() {
        val validation = FormValidator.validateFriend(name, budgetText)
        errors = validation
        val chosenBirthday = birthday
        birthdayError = if (chosenBirthday == null) "Выберите дату рождения" else null
        if (validation.hasErrors || chosenBirthday == null) return

        val friend = Friend(
            id = initial?.id ?: 0L,
            name = name.trim(),
            birthdayEpochDay = chosenBirthday,
            budget = FormValidator.parseMoney(budgetText) ?: 0.0,
            notes = notes.trim()
        )
        scope.launch {
            isSaving = true
            val saved = onSave(friend)
            isSaving = false
            // On an error the ViewModel has shown a message and the dialog stays open
            if (saved) onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (initial == null) "Новый друг" else "Редактирование друга") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errors = errors.copy(name = null)
                    },
                    label = { Text("Имя *") },
                    singleLine = true,
                    isError = errors.name != null,
                    supportingText = { if (errors.name != null) Text(errors.name.orEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    OutlinedButton(
                        onClick = { showPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(birthday?.let { "Дата рождения: ${DateUtils.formatFull(it)}" } ?: "Выбрать дату рождения *")
                    }
                    if (birthdayError != null) {
                        Text(
                            text = birthdayError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = {
                        budgetText = it
                        errors = errors.copy(budget = null)
                    },
                    label = { Text("Бюджет на подарки, $currencySymbol") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errors.budget != null,
                    supportingText = { if (errors.budget != null) Text(errors.budget.orEmpty()) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки (увлечения, размеры...)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { save() }, enabled = !isSaving) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Отмена") }
        }
    )

    if (showPicker) {
        BirthdayPickerDialog(
            initialEpochDay = birthday,
            onConfirm = {
                birthday = it
                birthdayError = null
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}
