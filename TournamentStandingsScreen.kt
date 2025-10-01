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
fun TournamentStandingsScreen(
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
            text = "📊 ТУРНИРНАЯ ТАБЛИЦА",
            color = Color(0xFF00FF88),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "В разработке",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(
            onClick = { navController.popBackStack() }
        ) {
            Text("Назад")
        }
    }
}