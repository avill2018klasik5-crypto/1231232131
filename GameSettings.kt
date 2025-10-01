package com.example.cs2battlegame.model

data class GameSettings(
    val autoPlayEnabled: Boolean = false,
    val roundDurationMs: Int = 1000, // 1 секунда по умолчанию
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)