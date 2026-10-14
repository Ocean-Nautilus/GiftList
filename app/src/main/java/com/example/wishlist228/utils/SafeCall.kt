package com.example.wishlist228.utils

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Runs a database operation on a background thread and turns any error into a [Result],
 * so that a failed save never crashes the app.
 */
suspend fun <T> safeDbCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(withContext(Dispatchers.IO) { block() })
    } catch (e: CancellationException) {
        // Never swallow cancellation: the coroutine must be able to stop
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

/** A short message for the user instead of a technical stack trace. */
fun Throwable.toUserMessage(): String = when (this) {
    is SQLiteException -> "Ошибка базы данных. Попробуйте ещё раз"
    else -> localizedMessage?.takeIf { it.isNotBlank() } ?: "Неизвестная ошибка"
}
