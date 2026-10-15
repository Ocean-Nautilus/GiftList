package com.example.wishlist228.utils

/**
 * Turns a saved amount back into the text shown in an input field:
 * 1500.0 becomes "1500", 99.5 stays "99.5", and 0 becomes an empty field.
 */
fun Double.toInputText(): String = when {
    this <= 0.0 -> ""
    this % 1.0 == 0.0 -> toLong().toString()
    else -> toString()
}

/** Adds [tag] to a comma separated list of tags unless it is already there. */
fun appendTag(currentText: String, tag: String): String {
    val existing = FormValidator.parseTags(currentText)
    if (existing.any { it.equals(tag, ignoreCase = true) }) return currentText
    return (existing + tag).joinToString(", ")
}
