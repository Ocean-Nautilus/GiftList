package com.example.wishlist228.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.wishlist228.models.AppSettings
import com.example.wishlist228.models.SortOrder
import com.example.wishlist228.models.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// One DataStore instance per file name, so it is declared once at the top level
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Reads and writes [AppSettings] (theme, default sort, currency, reminders). */
class SettingsStore(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val SORT = stringPreferencesKey("default_sort")
        val CURRENCY = stringPreferencesKey("currency_symbol")
        val REMINDERS = booleanPreferencesKey("reminders_enabled")
        val REMIND_DAYS = intPreferencesKey("remind_days_before")
    }

    val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            // A broken preferences file must not crash the app: fall back to defaults
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            val defaults = AppSettings()
            AppSettings(
                themeMode = prefs[Keys.THEME].toEnumOrDefault(defaults.themeMode),
                defaultSort = prefs[Keys.SORT].toEnumOrDefault(defaults.defaultSort),
                currencySymbol = prefs[Keys.CURRENCY] ?: defaults.currencySymbol,
                remindersEnabled = prefs[Keys.REMINDERS] ?: defaults.remindersEnabled,
                remindDaysBefore = prefs[Keys.REMIND_DAYS] ?: defaults.remindDaysBefore
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setDefaultSort(order: SortOrder) {
        dataStore.edit { it[Keys.SORT] = order.name }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { it[Keys.CURRENCY] = symbol }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REMINDERS] = enabled }
    }

    suspend fun setRemindDaysBefore(days: Int) {
        dataStore.edit { it[Keys.REMIND_DAYS] = days }
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
