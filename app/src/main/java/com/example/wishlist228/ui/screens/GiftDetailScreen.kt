package com.example.wishlist228.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.models.GiftWithTags
import com.example.wishlist228.ui.components.ConfirmDialog
import com.example.wishlist228.ui.components.EmptyState
import com.example.wishlist228.ui.components.GiftImage
import com.example.wishlist228.ui.components.LoadingBox
import com.example.wishlist228.ui.components.RatingBar
import com.example.wishlist228.ui.components.StatusChip
import com.example.wishlist228.utils.DateUtils
import com.example.wishlist228.utils.MoneyFormatter
import com.example.wishlist228.viewmodels.GiftDetailState
import com.example.wishlist228.viewmodels.GiftListViewModel
import kotlinx.coroutines.launch

/** Full information about one gift with the "Edit" and "Delete" buttons. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftDetailScreen(
    viewModel: GiftListViewModel,
    giftId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val detail by remember(giftId) { viewModel.observeGiftDetail(giftId) }
        .collectAsStateWithLifecycle(initialValue = GiftDetailState.Loading)
    val listState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var confirmDelete by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подарок") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        val current = detail
        when {
            isDeleting || current is GiftDetailState.Loading -> LoadingBox(Modifier.padding(padding))

            current is GiftDetailState.Loaded && current.gift == null -> EmptyState(
                icon = Icons.Filled.CardGiftcard,
                title = "Подарок не найден",
                message = "Возможно, он уже был удалён.",
                modifier = Modifier.padding(padding),
                actionLabel = "Назад",
                onAction = onBack
            )

            current is GiftDetailState.Loaded -> {
                val data = checkNotNull(current.gift)
                GiftDetailContent(
                    data = data,
                    friendName = listState.friends.firstOrNull { it.id == data.gift.friendId }?.name.orEmpty(),
                    currencySymbol = listState.currencySymbol,
                    onStatusChange = { viewModel.changeStatus(giftId, it) },
                    onLinkError = { viewModel.showMessage("Не удалось открыть ссылку") },
                    onEdit = { onEdit(giftId) },
                    onDelete = { confirmDelete = true },
                    modifier = Modifier.padding(padding)
                )

                if (confirmDelete) {
                    ConfirmDialog(
                        title = "Удалить подарок?",
                        text = "Вы уверены, что хотите удалить «${data.gift.title}»? Это действие нельзя отменить.",
                        confirmText = "Удалить",
                        onConfirm = {
                            confirmDelete = false
                            scope.launch {
                                isDeleting = true
                                if (viewModel.deleteGift(giftId)) {
                                    onBack()
                                } else {
                                    // The ViewModel has shown the reason, the gift stays on the screen
                                    isDeleting = false
                                }
                            }
                        },
                        onDismiss = { confirmDelete = false }
                    )
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun GiftDetailContent(
    data: GiftWithTags,
    friendName: String,
    currencySymbol: String,
    onStatusChange: (GiftStatus) -> Unit,
    onLinkError: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gift = data.gift
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GiftImage(
            url = gift.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Text(text = gift.title, style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusChip(status = gift.status)
            RatingBar(rating = gift.rating, starSize = 22.dp)
        }

        DetailRow(label = "Для кого", value = friendName.ifBlank { "—" })
        DetailRow(
            label = "Цена",
            value = if (gift.price > 0) MoneyFormatter.format(gift.price, currencySymbol) else "не указана"
        )
        DetailRow(label = "Добавлено", value = DateUtils.formatTimestamp(gift.createdAt))

        if (gift.description.isNotBlank()) {
            DetailRow(label = "Описание", value = gift.description)
        }

        if (gift.link.isNotBlank()) {
            Column {
                Text(
                    text = "Ссылка",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = gift.link,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        try {
                            uriHandler.openUri(gift.link)
                        } catch (e: Exception) {
                            onLinkError()
                        }
                    }
                )
            }
        }

        if (data.tags.isNotEmpty()) {
            DetailRow(label = "Теги", value = data.tags.joinToString("  ") { "#${it.name}" })
        }

        // A quick way to move the gift from "idea" to "bought" and "given"
        Text(
            text = "Статус",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GiftStatus.entries.forEach { option ->
                FilterChip(
                    selected = gift.status == option,
                    onClick = { onStatusChange(option) },
                    label = { Text(option.label) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Text("  Редактировать")
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Text("  Удалить")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
