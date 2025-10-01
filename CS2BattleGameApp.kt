package com.example.cs2battlegame

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs2battlegame.navigation.AppNavigation
import com.example.cs2battlegame.viewmodel.GameViewModel

@Composable
fun CS2BattleGameApp() {
    val gameViewModel: GameViewModel = viewModel()
    AppNavigation(gameViewModel)
}