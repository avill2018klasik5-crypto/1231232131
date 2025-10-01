package com.example.cs2battlegame.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = androidx.compose.ui.graphics.Color(0xFF00FF88),
    primaryVariant = androidx.compose.ui.graphics.Color(0xFF00CC6A),
    secondary = androidx.compose.ui.graphics.Color(0xFF6200EE),
    background = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
    surface = androidx.compose.ui.graphics.Color(0xFF2D2D2D),
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
)

@Composable
fun CS2BattleGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DarkColorPalette,
        typography = androidx.compose.material.Typography(),
        shapes = androidx.compose.material.Shapes(),
        content = content
    )
}