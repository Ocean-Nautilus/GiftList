package com.example.wishlist228.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.wishlist228.models.FriendBudget
import com.example.wishlist228.models.GiftStatus
import com.example.wishlist228.ui.theme.BudgetBarColor
import com.example.wishlist228.ui.theme.statusColor
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

/** Shows whole numbers on the chart (no "3.0" for three gifts). */
private val WholeNumberFormatter = object : ValueFormatter() {
    override fun getFormattedValue(value: Float): String = value.toInt().toString()
}

/**
 * Pie chart (MPAndroidChart): how many gifts are ideas, bought and already given.
 * Statuses without gifts are not drawn. Call it only when there is at least one gift.
 */
@Composable
fun StatusPieChart(counts: Map<GiftStatus, Int>, modifier: Modifier = Modifier) {
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val holeColor = MaterialTheme.colorScheme.surface.toArgb()
    val statuses = GiftStatus.entries.filter { (counts[it] ?: 0) > 0 }
    val total = statuses.sumOf { counts[it] ?: 0 }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setDrawEntryLabels(false)
                setTouchEnabled(false)
                isDrawHoleEnabled = true
                holeRadius = 55f
                transparentCircleRadius = 0f
                legend.apply {
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    textSize = 13f
                    isWordWrapEnabled = true
                }
            }
        },
        update = { chart ->
            val dataSet = PieDataSet(
                statuses.map { PieEntry((counts[it] ?: 0).toFloat(), it.label) },
                ""
            ).apply {
                setColors(statuses.map { statusColor(it).toArgb() })
                sliceSpace = 2f
                valueTextSize = 14f
                valueTextColor = Color.White.toArgb()
                valueFormatter = WholeNumberFormatter
            }
            chart.setHoleColor(holeColor)
            chart.setCenterText("$total")
            chart.setCenterTextSize(22f)
            chart.setCenterTextColor(labelColor)
            chart.legend.textColor = labelColor
            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}

/**
 * Grouped bar chart (MPAndroidChart): for every friend two bars,
 * the planned budget and the money already spent.
 */
@Composable
fun BudgetBarChart(budgets: List<FriendBudget>, modifier: Modifier = Modifier) {
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f).toArgb()
    val spentColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(false)
                setDrawGridBackground(false)
                axisRight.isEnabled = false
                legend.apply {
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    textSize = 13f
                }
            }
        },
        update = { chart ->
            // Bars of one friend are drawn next to each other:
            // (barWidth + barSpace) * 2 bars + groupSpace = 1.0 is the width of one group
            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f

            val budgetSet = BarDataSet(
                budgets.mapIndexed { index, item -> BarEntry(index.toFloat(), item.budget.toFloat()) },
                "Бюджет"
            ).apply {
                color = BudgetBarColor.toArgb()
                valueTextColor = labelColor
                valueTextSize = 9f
                valueFormatter = WholeNumberFormatter
            }
            val spentSet = BarDataSet(
                budgets.mapIndexed { index, item -> BarEntry(index.toFloat(), item.spent.toFloat()) },
                "Потрачено"
            ).apply {
                color = spentColor
                valueTextColor = labelColor
                valueTextSize = 9f
                valueFormatter = WholeNumberFormatter
            }

            val data = BarData(budgetSet, spentSet)
            data.barWidth = barWidth
            chart.data = data

            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(budgets.map { it.name })
                granularity = 1f
                isGranularityEnabled = true
                setCenterAxisLabels(true)
                setDrawGridLines(false)
                setLabelCount(budgets.size, false)
                axisMinimum = 0f
                axisMaximum = data.getGroupWidth(groupSpace, barSpace) * budgets.size
                textColor = labelColor
            }
            chart.axisLeft.apply {
                axisMinimum = 0f
                textColor = labelColor
                gridColor = gridLineColor
            }
            chart.legend.textColor = labelColor
            chart.groupBars(0f, groupSpace, barSpace)
            chart.invalidate()
        }
    )
}
