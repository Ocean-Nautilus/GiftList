package com.example.wishlist228.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wishlist228.models.StatsUiState
import com.example.wishlist228.ui.components.BudgetBarChart
import com.example.wishlist228.ui.components.EmptyState
import com.example.wishlist228.ui.components.LoadingBox
import com.example.wishlist228.ui.components.StatusPieChart
import com.example.wishlist228.utils.DateUtils
import com.example.wishlist228.utils.MoneyFormatter
import com.example.wishlist228.viewmodels.StatsViewModel
import java.util.Locale

/** Numbers and two charts (pie and bar) built from the real data of the user. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Статистика") }) }) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> LoadingBox()

                state.totalGifts == 0 -> EmptyState(
                    icon = Icons.Filled.BarChart,
                    title = "Пока нечего считать",
                    message = "Добавьте друзей и подарки, и здесь появятся графики и суммы."
                )

                else -> StatsContent(state)
            }
        }
    }
}

@Composable
private fun StatsContent(state: StatsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ----- Numbers -----
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Друзей", state.totalFriends.toString(), Modifier.weight(1f))
            StatCard("Подарков", state.totalGifts.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "Потрачено",
                MoneyFormatter.format(state.totalSpent, state.currencySymbol),
                Modifier.weight(1f)
            )
            StatCard(
                "В планах",
                MoneyFormatter.format(state.plannedSpend, state.currencySymbol),
                Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "Средний рейтинг",
                String.format(Locale.getDefault(), "%.1f из 5", state.averageRating),
                Modifier.weight(1f)
            )
            StatCard(
                "Ближайший праздник",
                if (state.nextBirthdayName != null && state.nextBirthdayDays != null) {
                    "${state.nextBirthdayName}: ${DateUtils.countdownText(state.nextBirthdayDays)}"
                } else {
                    "—"
                },
                Modifier.weight(1f)
            )
        }

        // ----- Pie chart: gifts by status -----
        Text(
            text = "Подарки по статусам",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        StatusPieChart(
            counts = state.statusCounts,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        // ----- Bar chart: budget and spent money of every friend -----
        if (state.friendBudgets.isNotEmpty()) {
            Text(
                text = "Бюджет и расходы по друзьям, ${state.currencySymbol}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            BudgetBarChart(
                budgets = state.friendBudgets,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
