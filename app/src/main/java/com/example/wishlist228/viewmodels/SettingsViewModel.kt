package com.example.wishlist228.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlist228.GiftListApplication
import com.example.wishlist228.models.AppSettings
import com.example.wishlist228.models.SortOrder
import com.example.wishlist228.models.ThemeMode
import com.example.wishlist228.utils.ReminderScheduler
import com.example.wishlist228.utils.safeDbCall
import com.example.wishlist228.utils.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Logic of the "Settings" screen: preferences, reminders and the demo data. */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val container = (application as GiftListApplication).container
    private val settingsStore = container.settingsStore

    private val eventChannel = Channel<String>(Channel.BUFFERED)
    val events: Flow<String> = eventChannel.receiveAsFlow()

    // Eagerly: the theme must be known as soon as the app starts
    val settings: StateFlow<AppSettings> =
        settingsStore.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setThemeMode(mode: ThemeMode) = save { settingsStore.setThemeMode(mode) }

    fun setDefaultSort(order: SortOrder) = save { settingsStore.setDefaultSort(order) }

    fun setCurrency(symbol: String) = save { settingsStore.setCurrencySymbol(symbol) }

    fun setRemindDaysBefore(days: Int) = save { settingsStore.setRemindDaysBefore(days) }

    /** Turns the daily birthday check on or off. */
    fun setRemindersEnabled(enabled: Boolean) = save {
        settingsStore.setRemindersEnabled(enabled)
        if (enabled) ReminderScheduler.schedule(app) else ReminderScheduler.cancel(app)
    }

    /** Shows a short message in the Snackbar of the main screen. */
    fun showMessage(text: String) {
        eventChannel.trySend(text)
    }

    /** Runs the reminder check immediately, so that the user can see how it looks. */
    fun sendTestNotification() {
        try {
            ReminderScheduler.runNow(app)
            eventChannel.trySend("Проверка запущена, уведомление появится через пару секунд")
        } catch (e: Exception) {
            eventChannel.trySend("Не удалось запустить проверку: ${e.toUserMessage()}")
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            val result = safeDbCall { container.demoData.insert() }
            eventChannel.trySend(
                result.exceptionOrNull()?.let { "Не удалось добавить демо-данные: ${it.toUserMessage()}" }
                    ?: "Демо-данные добавлены"
            )
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val result = safeDbCall { container.repository.clearAll() }
            eventChannel.trySend(
                result.exceptionOrNull()?.let { "Не удалось удалить данные: ${it.toUserMessage()}" }
                    ?: "Все данные удалены"
            )
        }
    }

    private fun save(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                eventChannel.trySend("Не удалось сохранить настройку: ${e.toUserMessage()}")
            }
        }
    }
}
