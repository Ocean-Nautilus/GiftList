package com.example.ohmywishes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Фирменная палитра OhMyWishes
val OhMyPink = Color(0xFFFF3B6B)
val OhMyPinkLight = Color(0xFFFFF0F4)
val OhMyPurple = Color(0xFF7C4DFF)
val BackgroundLight = Color(0xFFF7F8FA)
val SurfaceCard = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1E1E24)
val TextSecondary = Color(0xFF8E8E93)
val ReservedColor = Color(0xFF4CAF50)

private val LightColorScheme = lightColorScheme(
    primary = OhMyPink,
    secondary = OhMyPurple,
    background = BackgroundLight,
    surface = SurfaceCard,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun OhMyWishesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}#!/usr/bin/env kotlin

