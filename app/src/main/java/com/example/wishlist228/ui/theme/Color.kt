package com.example.wishlist228.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.wishlist228.models.GiftStatus

// Brand palette of the app: pink and purple
val BrandPink = Color(0xFFFF3B6B)
val BrandPinkLight = Color(0xFFFF8FAA)
val BrandPurple = Color(0xFF7C4DFF)
val BrandPurpleLight = Color(0xFFB9A2FF)

// Colors of the gift statuses: used by the chips and by the pie chart
val StatusIdeaColor = Color(0xFF7C4DFF)
val StatusPurchasedColor = Color(0xFFF29900)
val StatusGivenColor = Color(0xFF2E9E5B)

// Other colors
val StarColor = Color(0xFFFFB300)
val BudgetBarColor = Color(0xFF9AA0A6)

/** The color that stands for a gift status everywhere in the app. */
fun statusColor(status: GiftStatus): Color = when (status) {
    GiftStatus.IDEA -> StatusIdeaColor
    GiftStatus.PURCHASED -> StatusPurchasedColor
    GiftStatus.GIVEN -> StatusGivenColor
}
