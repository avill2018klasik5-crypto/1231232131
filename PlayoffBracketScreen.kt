package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.viewmodel.GameViewModel

@Composable
fun PlayoffBracketScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val playoffBracket = gameViewModel.playoffBracket.collectAsState().value
    val selectedTeam = gameViewModel.gameState.value.selectedTeam
    val tournament = gameViewModel.selectedTournament.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Text(
            text = "🏆 СЕТКА ПЛЕЙ-ОФФ",
            color = Color(0xFFFFD700),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = tournament?.name ?: "Турнир",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ОБНОВЛЕННОЕ ОТОБРАЖЕНИЕ: Показываем чемпиона, если турнир завершен
        if (playoffBracket?.champion != null) {
            ChampionDisplay(champion = playoffBracket.champion!!)
        }

        if (playoffBracket != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Четвертьфиналы
                item {
                    PlayoffRoundSection(
                        title = "ЧЕТВЕРТЬФИНАЛЫ",
                        matches = playoffBracket.quarterFinals,
                        selectedTeam = selectedTeam,
                        gameViewModel = gameViewModel,
                        navController = navController
                    )
                }

                // Полуфиналы
                item {
                    PlayoffRoundSection(
                        title = "ПОЛУФИНАЛЫ",
                        matches = playoffBracket.semiFinals,
                        selectedTeam = selectedTeam,
                        gameViewModel = gameViewModel,
                        navController = navController
                    )
                }

                // Финал
                item {
                    playoffBracket.final?.let { final ->
                        PlayoffRoundSection(
                            title = "ФИНАЛ",
                            matches = listOf(final),
                            selectedTeam = selectedTeam,
                            gameViewModel = gameViewModel,
                            navController = navController
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сетка плей-офф еще не сформирована",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки управления
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val nextPlayerMatch = gameViewModel.getPlayerPlayoffMatch()

            if (nextPlayerMatch != null && !nextPlayerMatch.isCompleted) {
                Button(
                    onClick = {
                        gameViewModel.startPlayoffMatch(nextPlayerMatch)
                        navController.navigate("playoff_battle")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FF88))
                ) {
                    Text("🎮 ИГРАТЬ СЛЕДУЮЩИЙ МАТЧ", fontSize = 16.sp)
                }
            }

            // ОБНОВЛЕННАЯ КНОПКА: Всегда показываем кнопку возврата в групповой этап
            Button(
                onClick = {
                    gameViewModel.saveCurrentMatchState()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
            ) {
                Text("← НАЗАД К ГРУППОВОМУ ЭТАПУ", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ChampionDisplay(champion: com.example.cs2battlegame.model.Team) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        backgroundColor = Color(0xFFFFD700).copy(alpha = 0.2f),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 ЧЕМПИОН ТУРНИРА 🏆",
                color = Color(0xFFFFD700),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = champion.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = champion.country,
                color = Color(0xFF00FF88),
                fontSize = 16.sp
            )
            Text(
                text = "Поздравляем победителя!",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun PlayoffRoundSection(
    title: String,
    matches: List<com.example.cs2battlegame.model.PlayoffMatch>,
    selectedTeam: com.example.cs2battlegame.model.Team?,
    gameViewModel: GameViewModel,
    navController: NavHostController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF2D2D2D),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            matches.forEach { match ->
                PlayoffMatchItem(
                    match = match,
                    selectedTeam = selectedTeam,
                    gameViewModel = gameViewModel,
                    navController = navController
                )
                if (match != matches.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PlayoffMatchItem(
    match: com.example.cs2battlegame.model.PlayoffMatch,
    selectedTeam: com.example.cs2battlegame.model.Team?,
    gameViewModel: GameViewModel,
    navController: NavHostController
) {
    val isPlayerMatch = selectedTeam?.let {
        match.team1 == it || match.team2 == it
    } ?: false

    val backgroundColor = when {
        match.isCompleted -> Color(0xFF2A5C2A)
        isPlayerMatch -> Color(0xFF1A237E)
        else -> Color(0xFF424242)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = backgroundColor,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.team1?.name ?: "TBD",
                        color = if (match.winner == match.team1) Color(0xFF00FF88) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = match.team2?.name ?: "TBD",
                        color = if (match.winner == match.team2) Color(0xFF00FF88) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    if (match.isCompleted) {
                        Text(
                            text = "${match.team1Score}:${match.team2Score}",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (match.winner == selectedTeam) "✅ ВАША ПОБЕДА"
                            else if (isPlayerMatch) "❌ ВЫБЫЛИ"
                            else "ЗАВЕРШЕН",
                            color = if (match.winner == selectedTeam) Color(0xFF00FF88) else Color.Gray,
                            fontSize = 10.sp
                        )
                    } else {
                        if (isPlayerMatch) {
                            Text(
                                text = "ОЖИДАЕТСЯ",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "НЕ НАЧАТ",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (!match.isCompleted && isPlayerMatch) {
                Button(
                    onClick = {
                        gameViewModel.startPlayoffMatch(match)
                        navController.navigate("playoff_battle")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FF88))
                ) {
                    Text("ИГРАТЬ МАТЧ", fontSize = 14.sp)
                }
            }
        }
    }
}