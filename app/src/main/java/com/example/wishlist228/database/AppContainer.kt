package com.example.wishlist228.database

import android.content.Context

/**
 * Very small "manual dependency injection": creates the long-living objects once
 * and gives them to ViewModels and workers.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.create(appContext) }
    val repository: GiftRepository by lazy { GiftRepository(database) }
    val demoData: DemoData by lazy { DemoData(repository) }
    val settingsStore: SettingsStore by lazy { SettingsStore(appContext) }
}
