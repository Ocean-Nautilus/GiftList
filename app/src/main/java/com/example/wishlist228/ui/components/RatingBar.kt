package com.example.wishlist228.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wishlist228.ui.theme.StarColor

/**
 * Stars from 1 to [maxRating]. When [onRatingChange] is given, the user can tap a star
 * to change the rating (the add/edit form); without it the stars are only shown.
 */
@Composable
fun RatingBar(
    rating: Int,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 18.dp,
    onRatingChange: ((Int) -> Unit)? = null
) {
    Row(modifier = modifier) {
        for (star in 1..maxRating) {
            val starModifier = if (onRatingChange != null) {
                Modifier
                    .size(starSize)
                    .clickable { onRatingChange(star) }
            } else {
                Modifier.size(starSize)
            }
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Оценка $star из $maxRating",
                tint = StarColor,
                modifier = starModifier
            )
        }
    }
}
