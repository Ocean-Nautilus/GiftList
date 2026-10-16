package com.example.wishlist228

import android.app.Application
import android.util.Log
import com.example.wishlist228.database.AppContainer
import com.example.wishlist228.utils.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Application class: owns the [AppContainer] with the database and the settings. */
class GiftListApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Restore the daily reminder check after the app (or the device) was restarted
        applicationScope.launch {
            try {
                if (container.settingsStore.settings.first().remindersEnabled) {
                    ReminderScheduler.schedule(this@GiftListApplication)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not restore reminders", e)
            }
        }
    }

    private companion object {
        const val TAG = "GiftListApplication"
    }
}
