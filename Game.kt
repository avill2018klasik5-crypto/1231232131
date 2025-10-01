package com.example.cs2battlegame.model

// Состояние игры
enum class GameState {
    MENU,       // Главное меню
    RANKING,    // Рейтинг команд
    BATTLE,     // Битва
    RESULTS     // Результаты
}

data class Game(
    var state: GameState = GameState.MENU,
    var currentBattle: Battle? = null,
    var selectedTeam: Team? = null
)