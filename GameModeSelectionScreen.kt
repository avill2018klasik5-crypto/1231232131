package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.viewmodel.GameViewModel

@Composable
fun GameModeSelectionScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎮 ВЫБОР РЕЖИМА ИГРЫ",
            color = Color(0xFF00FF88),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Button(
            onClick = {
                navController.navigate("tournament_selection")
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FF88))
        ) {
            Text("🏆 ИГРАТЬ ТУРНИР", fontSize = 18.sp)
        }

        Button(
            onClick = {
                navController.navigate("single_match_team_selection")
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
        ) {
            Text("⚔️ ОБЫЧНЫЙ МАТЧ", fontSize = 18.sp)
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
        ) {
            Text("← НАЗАД В МЕНЮ", fontSize = 18.sp)
        }
    }
}