package com.example.wishlist228.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * A read-only text field that opens a menu with [options] when it is tapped
 * (used for choosing a friend in the form and the default sort order in the settings).
 */
@Composable
fun <T> DropdownField(
    label: String,
    selectedText: String,
    options: List<T>,
    optionText: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            isError = errorText != null,
            supportingText = { if (errorText != null) Text(errorText) },
            modifier = Modifier.fillMaxWidth()
        )
        // A transparent layer on top: the read-only field itself would not react to taps
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionText(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * A filter chip that opens a menu. While nothing is chosen the chip shows [title],
 * after a choice it shows the name of the chosen option. The first menu item resets the filter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> FilterChipMenu(
    title: String,
    selectedLabel: String?,
    options: List<Pair<T, String>>,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            selected = selectedLabel != null,
            onClick = { expanded = true },
            label = { Text(selectedLabel ?: title) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Все") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
