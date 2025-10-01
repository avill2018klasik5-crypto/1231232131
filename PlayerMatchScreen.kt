package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import com.example.cs2battlegame.model.MapPool
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.viewmodel.GameViewModel
import kotlin.random.Random

@Composable
fun PlayerMatchScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val currentMatch by gameViewModel.currentPlayerMatch.collectAsState()
    val playerTeam = gameViewModel.gameState.value.selectedTeam
    var playerMapsWon by remember { mutableStateOf(0) }
    var opponentMapsWon by remember { mutableStateOf(0) }
    var currentMap by remember { mutableStateOf(1) }
    var playerScore by remember { mutableStateOf(0) }
    var opponentScore by remember { mutableStateOf(0) }
    var isMatchFinished by remember { mutableStateOf(false) }
    val mapResults = remember { mutableListOf<com.example.cs2battlegame.model.MapResult>() }

    LaunchedEffect(Unit) {
        // Симулируем только один раунд матчей вместо всех
        gameViewModel.simulateOneRoundGroupMatches()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "🎮 ВАШ МАТЧ BO3",
            color = Color(0xFF00FF88),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        // Информация о матче
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF2D2D2D)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${playerTeam?.name ?: "Ваша команда"} vs ${currentMatch?.opponent?.name ?: "Соперник"}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Карта $currentMap/3 • BO3",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Счет по картам: $playerMapsWon - $opponentMapsWon",
                    color = Color(0xFFFFD700),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Отображаем силу команд
                playerTeam?.let { pt ->
                    currentMatch?.opponent?.let { op ->
                        Text(
                            text = "Сила команд: ${pt.getDisplayStrength()} vs ${op.getDisplayStrength()}",
                            color = Color(0xFF00FF88),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Текущая карта
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF2D2D2D)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "КАРТА $currentMap",
                    color = Color(0xFFFF9800),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Счет: ${playerTeam?.name ?: "Вы"} $playerScore - ${opponentScore} ${currentMatch?.opponent?.name ?: "Соперник"}",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = "До победы: ${13 - maxOf(playerScore, opponentScore)} раундов",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!isMatchFinished) {
            // === ОБНОВЛЕННАЯ КНОПКА: Симуляция раунда с учетом силы ===
            Button(
                onClick = {
                    // Рассчитываем вероятность победы игрока на основе силы команд
                    val playerTeam = playerTeam ?: return@Button
                    val opponentTeam = currentMatch?.opponent ?: return@Button

                    val playerStrength = playerTeam.getDisplayStrength().toDouble()
                    val opponentStrength = opponentTeam.getDisplayStrength().toDouble()
                    val totalStrength = playerStrength + opponentStrength

                    // Базовая вероятность 50%, корректируется разницей в силе
                    val baseProbability = 0.5
                    val strengthDifference = (playerStrength - opponentStrength) / totalStrength
                    val winProbability = baseProbability + (strengthDifference * 0.4)

                    // Симуляция раунда с учетом силы
                    if (Random.nextDouble() < winProbability) {
                        playerScore++
                    } else {
                        opponentScore++
                    }

                    // Проверяем победу на карте (ФИКС: до 13 раундов)
                    if (playerScore >= 13 || opponentScore >= 13) {
                        val mapPool = currentMatch?.maps?.getOrNull(currentMap - 1)
                        val mapName = mapPool?.getDisplayName() ?: "Карта $currentMap"
                        val winner = if (playerScore > opponentScore) playerTeam else currentMatch?.opponent

                        mapResults.add(com.example.cs2battlegame.model.MapResult(
                            mapNumber = currentMap,
                            mapName = mapName,
                            team1Score = playerScore,
                            team2Score = opponentScore,
                            winner = winner
                        ))

                        if (playerScore > opponentScore) {
                            playerMapsWon++
                        } else {
                            opponentMapsWon++
                        }

                        // Сбрасываем счет для следующей карты
                        playerScore = 0
                        opponentScore = 0
                        currentMap++

                        // Проверяем окончание матча BO3
                        if (playerMapsWon == 2 || opponentMapsWon == 2) {
                            isMatchFinished = true
                            gameViewModel.completePlayerMatch(playerMapsWon, opponentMapsWon, mapResults)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FF88))
            ) {
                Text("🎯 СИМУЛИРОВАТЬ РАУНД", fontSize = 16.sp)
            }

            // === ОБНОВЛЕННАЯ КНОПКА: Быстрая симуляция карты с учетом силы ===
            Button(
                onClick = {
                    // Рассчитываем вероятность победы игрока на основе силы команд
                    val playerTeam = playerTeam ?: return@Button
                    val opponentTeam = currentMatch?.opponent ?: return@Button

                    val playerStrength = playerTeam.getDisplayStrength().toDouble()
                    val opponentStrength = opponentTeam.getDisplayStrength().toDouble()
                    val totalStrength = playerStrength + opponentStrength

                    // Базовая вероятность 50%, корректируется разницей в силе
                    val baseProbability = 0.5
                    val strengthDifference = (playerStrength - opponentStrength) / totalStrength
                    val winProbability = baseProbability + (strengthDifference * 0.4)

                    // Быстрая симуляция текущей карты с учетом силы
                    val mapPool = currentMatch?.maps?.getOrNull(currentMap - 1)
                    val mapName = mapPool?.getDisplayName() ?: "Карта $currentMap"

                    // Используем вероятность на основе силы для определения победителя карты
                    val mapWinner = Random.nextDouble() < winProbability

                    // ФИКС: Правильный счет до 13 раундов
                    if (mapWinner) {
                        playerScore = 13
                        opponentScore = Random.nextInt(0, 13) // 0-12
                        playerMapsWon++
                    } else {
                        opponentScore = 13
                        playerScore = Random.nextInt(0, 13) // 0-12
                        opponentMapsWon++
                    }

                    val winner = if (mapWinner) playerTeam else currentMatch?.opponent
                    mapResults.add(com.example.cs2battlegame.model.MapResult(
                        mapNumber = currentMap,
                        mapName = mapName,
                        team1Score = playerScore,
                        team2Score = opponentScore,
                        winner = winner
                    ))

                    playerScore = 0
                    opponentScore = 0
                    currentMap++

                    if (playerMapsWon == 2 || opponentMapsWon == 2) {
                        isMatchFinished = true
                        gameViewModel.completePlayerMatch(playerMapsWon, opponentMapsWon, mapResults)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
            ) {
                Text("⚡ БЫСТРАЯ СИМУЛЯЦИЯ КАРТЫ", fontSize = 16.sp)
            }
        } else {
            // Результат матча BO3
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (playerMapsWon > opponentMapsWon) Color(0xFF2A5C2A) else Color(0xFF5C2A2A)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (playerMapsWon > opponentMapsWon) "🏆 ПОБЕДА В МАТЧЕ!" else "💔 ПОРАЖЕНИЕ В МАТЧЕ",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Финальный счет BO3: $playerMapsWon - $opponentMapsWon",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    // Показываем результаты карт
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Результаты карт:",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    mapResults.forEach { mapResult ->
                        Text(
                            text = "${mapResult.mapName}: ${mapResult.team1Score}:${mapResult.team2Score}",
                            color = if (mapResult.winner == playerTeam) Color(0xFF00FF88) else Color(0xFFFF4444),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (gameViewModel.startNextPlayerMatch()) {
                                navController.navigate("player_match")
                            } else {
                                navController.navigate("group_stage")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
                    ) {
                        Text("➡️ ДАЛЕЕ", fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
        ) {
            Text("← НАЗАД К ГРУППЕ", fontSize = 16.sp)
        }
    }
}