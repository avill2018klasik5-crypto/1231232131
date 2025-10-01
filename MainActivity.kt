package com.example.cs2battlegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cs2battlegame.navigation.AppNavigation
import com.example.cs2battlegame.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Обработка непредвиденных ошибок
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            android.util.Log.e("CS2BattleGame", "Uncaught exception: ${throwable.message}")
            throwable.printStackTrace()
        }

        try {
            setContent {
                MaterialTheme {
                    Surface {
                        val gameViewModel: GameViewModel = viewModel()
                        AppNavigation(gameViewModel)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CS2BattleGame", "Error in setContent: ${e.message}")
            e.printStackTrace()
        }
    }
}