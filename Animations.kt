package com.example.cs2battlegame.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

// Простая пульсирующая анимация
@Composable
fun rememberPulseAnimation(isActive: Boolean): Float {
    val infiniteTransition = rememberInfiniteTransition()
    return if (isActive) {
        val pulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
        pulse
    } else {
        1f
    }
}

// Анимация масштабирования
fun Modifier.scaleAnimation(scale: Float): Modifier = composed {
    this.scale(scale)
}