package com.example.wishlist228.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wishlist228.models.AppSettings
import com.example.wishlist228.models.SortOrder
import com.example.wishlist228.models.ThemeMode
import com.example.wishlist228.ui.components.ConfirmDialog
import com.example.wishlist228.ui.components.DropdownField
import com.example.wishlist228.utils.DateUtils
import com.example.wishlist228.utils.NotificationHelper
import com.example.wishlist228.viewmodels.SettingsViewModel

/** What the app wanted to do when it asked for the notification permission. */
private enum class PendingAction { NONE, ENABLE_REMINDERS, SEND_TEST }

/**
 * Settings: theme, default sort order, currency, birthday reminders (the "feature" of the app)
 * and the demo data. Every change is saved at once.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var confirmClear by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf(PendingAction.NONE) }

    // Android 13+ asks the user whether the app may show notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingAction
        pendingAction = PendingAction.NONE
        if (!granted) {
            viewModel.showMessage("Без разрешения на уведомления напоминания не будут показываться")
        } else {
            when (action) {
                PendingAction.ENABLE_REMINDERS -> viewModel.setRemindersEnabled(true)
                PendingAction.SEND_TEST -> viewModel.sendTestNotification()
                PendingAction.NONE -> Unit
            }
        }
    }

    /** Runs [action] at once if notifications are allowed, otherwise asks for the permission first. */
    fun withNotificationPermission(action: PendingAction, run: () -> Unit) {
        if (NotificationHelper.canPostNotifications(context)) {
            run()
        } else {
            pendingAction = action
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Настройки") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = "Оформление") {
                Text("Тема", style = MaterialTheme.typography.labelLarge)
                ChipRow {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(mode.label) }
                        )
                    }
                }
            }

            SettingsSection(title = "Список подарков") {
                DropdownField(
                    label = "Сортировка по умолчанию",
                    selectedText = settings.defaultSort.label,
                    options = SortOrder.entries,
                    optionText = { it.label },
                    onSelect = { viewModel.setDefaultSort(it) }
                )
                Text("Валюта", style = MaterialTheme.typography.labelLarge)
                ChipRow {
                    AppSettings.CURRENCIES.forEach { symbol ->
                        FilterChip(
                            selected = settings.currencySymbol == symbol,
                            onClick = { viewModel.setCurrency(symbol) },
                            label = { Text(symbol) }
                        )
                    }
                }
            }

            SettingsSection(title = "Напоминания о днях рождения") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Напоминать заранее, чтобы успеть выбрать подарок",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.remindersEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                withNotificationPermission(PendingAction.ENABLE_REMINDERS) {
                                    viewModel.setRemindersEnabled(true)
                                }
                            } else {
                                viewModel.setRemindersEnabled(false)
                            }
                        }
                    )
                }
                if (settings.remindersEnabled) {
                    Text("Напоминать за", style = MaterialTheme.typography.labelLarge)
                    ChipRow {
                        AppSettings.REMIND_DAYS_OPTIONS.forEach { days ->
                            FilterChip(
                                selected = settings.remindDaysBefore == days,
                                onClick = { viewModel.setRemindDaysBefore(days) },
                                label = { Text("$days ${DateUtils.pluralDays(days)}") }
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            withNotificationPermission(PendingAction.SEND_TEST) {
                                viewModel.sendTestNotification()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Показать тестовое уведомление") }
                }
            }

            SettingsSection(title = "Данные") {
                OutlinedButton(
                    onClick = { viewModel.loadDemoData() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Добавить демо-данные") }
                Button(
                    onClick = { confirmClear = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Удалить все данные") }
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "Удалить все данные?",
            text = "Будут удалены все друзья и подарки. Это действие нельзя отменить.",
            confirmText = "Удалить всё",
            onConfirm = {
                confirmClear = false
                viewModel.clearAllData()
            },
            onDismiss = { confirmClear = false }
        )
    }
}

/** A card with a title that groups related settings. */
@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

/** A row of chips that scrolls sideways when it does not fit on a narrow screen. */
@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}
