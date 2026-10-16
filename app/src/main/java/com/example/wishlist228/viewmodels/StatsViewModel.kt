package com.example.wishlist228.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wishlist228.GiftListApplication
import com.example.wishlist228.models.StatsUiState
import com.example.wishlist228.utils.StatsCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Provides the statistics screen with numbers and chart data computed from the database. */
class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as GiftListApplication).container
    private val repository = container.repository

    val uiState: StateFlow<StatsUiState> = combine(
        repository.friends,
        repository.gifts,
        container.settingsStore.settings
    ) { friends, gifts, settings ->
        StatsCalculator.calculate(friends, gifts, settings.currencySymbol)
    }.catch {
        // Statistics are not critical: show an empty screen instead of crashing
        emit(StatsUiState(isLoading = false))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
