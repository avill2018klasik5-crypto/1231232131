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
fun TournamentOverviewScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val selectedTournament = gameViewModel.selectedTournament.value
    val selectedTeam = gameViewModel.gameState.value.selectedTeam
    val playoffBracket = gameViewModel.playoffBracket.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "🏆 ОБЗОР ТУРНИРА",
            color = Color(0xFF00FF88),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Информация о турнире
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF2D2D2D),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = selectedTournament?.name ?: "Турнир",
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                TournamentInfoRow("🎯 Организатор", selectedTournament?.organizer ?: "")
                TournamentInfoRow("📍 Место", selectedTournament?.location ?: "")
                TournamentInfoRow("📅 Дата", selectedTournament?.date ?: "")
                TournamentInfoRow("💰 Призовой фонд", selectedTournament?.prizePool ?: "")
                TournamentInfoRow("⚔️ Формат", selectedTournament?.format ?: "")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ваша команда: ${selectedTeam?.name ?: "Не выбрана"}",
                    color = Color(0xFF00FF88),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Статус турнира
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        playoffBracket?.champion != null -> "🏆 Турнир завершен"
                        playoffBracket != null -> "⚔️ Стадия плей-офф"
                        else -> "📊 Групповая стадия"
                    },
                    color = when {
                        playoffBracket?.champion != null -> Color(0xFFFFD700)
                        playoffBracket != null -> Color(0xFFFF9800)
                        else -> Color(0xFF00FF88)
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Информация о чемпионе
                playoffBracket?.champion?.let { champion ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Чемпион: ${champion.name}",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Кнопки управления турниром
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    navController.navigate("group_stage")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FF88))
            ) {
                Text("📊 ГРУППОВОЙ ЭТАП", fontSize = 16.sp)
            }

            // Кнопка плей-офф (если доступна)
            if (playoffBracket != null) {
                Button(
                    onClick = {
                        navController.navigate("playoff_bracket")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
                ) {
                    Text("🏆 СЕТКА ПЛЕЙ-ОФФ", fontSize = 16.sp)
                }
            }

            // Дополнительная информация о прогрессе
            if (playoffBracket != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF2A2A2A),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎯 Прогресс плей-офф",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val totalMatches = listOfNotNull(
                            playoffBracket.quarterFinals.size,
                            playoffBracket.semiFinals.size,
                            playoffBracket.final?.let { 1 }
                        ).sum()

                        val completedMatches = listOfNotNull(
                            playoffBracket.quarterFinals.count { it.isCompleted },
                            playoffBracket.semiFinals.count { it.isCompleted },
                            playoffBracket.final?.let { if (it.isCompleted) 1 else 0 }
                        ).sum()

                        Text(
                            text = "Матчи: $completedMatches/$totalMatches",
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        if (playoffBracket.champion != null) {
                            Text(
                                text = "Чемпион: ${playoffBracket.champion?.name}",
                                color = Color(0xFF00FF88),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка назад
        Button(
            onClick = {
                navController.popBackStack("tournament_selection", false)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
        ) {
            Text("← ВЫБРАТЬ ДРУГОЙ ТУРНИР", fontSize = 16.sp)
        }
    }
}

@Composable
fun TournamentInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}