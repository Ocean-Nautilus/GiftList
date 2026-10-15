package com.example.wishlist228.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import java.time.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * A calendar dialog for choosing a birth date.
 * The date is returned as `LocalDate.toEpochDay()`, the same format that the database uses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayPickerDialog(
    initialEpochDay: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    val startDay = initialEpochDay ?: today.minusYears(25).toEpochDay()
    // The Material date picker works with UTC midnight in milliseconds
    val state = rememberDatePickerState(
        initialSelectedDateMillis = startDay * MILLIS_PER_DAY,
        yearRange = 1900..today.year
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) onConfirm(Math.floorDiv(millis, MILLIS_PER_DAY)) else onDismiss()
                }
            ) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    ) {
        DatePicker(state = state)
    }
}
