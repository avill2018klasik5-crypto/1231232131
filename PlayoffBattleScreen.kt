package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.utils.SafeTeamLogo
import com.example.cs2battlegame.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun PlayoffBattleScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val gameState by gameViewModel.gameState.collectAsState()
    val battle by remember { derivedStateOf { gameState.currentBattle } }
    val playoffMatch by gameViewModel.currentPlayoffMatch.collectAsState()
    val selectedTeam by remember { derivedStateOf { gameState.selectedTeam } }

    var currentRoundResult by remember { mutableStateOf<com.example.cs2battlegame.model.RoundResult?>(null) }
    var forceUpdate by remember { mutableStateOf(0) }

    // Сохраняем состояние при каждом изменении
    LaunchedEffect(battle, currentRoundResult) {
        gameViewModel.saveCurrentMatchState()
    }

    // Сохраняем состояние при выходе
    DisposableEffect(Unit) {
        onDispose {
            gameViewModel.saveCurrentMatchState()
        }
    }

    LaunchedEffect(battle) {
        if (battle != null && battle!!.isSeriesFinished) {
            delay(1000)
            currentRoundResult = com.example.cs2battlegame.model.RoundResult.SERIES_FINISHED
            forceUpdate++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        if (battle == null || playoffMatch == null || selectedTeam == null) {
            PlayoffBattleErrorScreen(navController)
        } else {
            PlayoffBattleInterface(
                battle = battle!!,
                playoffMatch = playoffMatch!!,
                selectedTeam = selectedTeam!!,
                gameViewModel = gameViewModel,
                navController = navController,
                currentRoundResult = currentRoundResult,
                forceUpdate = forceUpdate,
                onRoundResult = { result ->
                    currentRoundResult = result
                    forceUpdate++
                }
            )
        }
    }
}

@Composable
fun PlayoffBattleErrorScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌ Матч плей-офф не начат",
            color = Color.Red,
            fontSize = 20.sp
        )
        Button(
            onClick = {
                navController.popBackStack("playoff_bracket", false)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Вернуться к сетке плей-офф")
        }
    }
}

@Composable
fun PlayoffBattleInterface(
    battle: com.example.cs2battlegame.model.Battle,
    playoffMatch: com.example.cs2battlegame.model.PlayoffMatch,
    selectedTeam: com.example.cs2battlegame.model.Team,
    gameViewModel: GameViewModel,
    navController: NavHostController,
    currentRoundResult: com.example.cs2battlegame.model.RoundResult?,
    forceUpdate: Int,
    onRoundResult: (com.example.cs2battlegame.model.RoundResult) -> Unit
) {
    val isSeriesFinished = battle.isSeriesFinished

    LaunchedEffect(forceUpdate) {
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (isSeriesFinished) "🏆 СЕРИЯ ЗАВЕРШЕНА" else "🏆 ПЛЕЙ-ОФФ МАТЧ BO3",
                color = if (isSeriesFinished) Color(0xFFFFD700) else Color(0xFFFF4444),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Стадия: ${getPlayoffStage(playoffMatch.matchId)}",
                color = Color(0xFF00FF88),
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Карты: ${battle.team1Maps}:${battle.team2Maps}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = battle.getCurrentMapName(),
                color = Color(0xFFFF9800),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayoffTeamScoreDisplay(
                    team = battle.team1,
                    score = battle.team1Score,
                    isAttacking = battle.isTeam1Attacking,
                    isSelected = battle.team1 == selectedTeam,
                    onTeamClick = { gameViewModel.showTeamStats(battle.team1) }
                )

                Text(
                    text = "VS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                PlayoffTeamScoreDisplay(
                    team = battle.team2,
                    score = battle.team2Score,
                    isAttacking = !battle.isTeam1Attacking,
                    isSelected = battle.team2 == selectedTeam,
                    onTeamClick = { gameViewModel.showTeamStats(battle.team2) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = battle.getRoundInfo(),
                color = Color(0xFF00FF88),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            PlayoffPlayersStatistics(battle)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            if (!isSeriesFinished) {
                PlayoffBattleControls(
                    battle = battle,
                    onSimulateRound = {
                        val result = battle.simulateRound()
                        onRoundResult(result)
                    },
                    onSimulateSeries = {
                        battle.simulateFullSeries()
                        onRoundResult(com.example.cs2battlegame.model.RoundResult.SERIES_FINISHED)
                    }
                )
            } else {
                PlayoffSeriesFinishedControls(navController, gameViewModel, battle, playoffMatch)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    gameViewModel.saveCurrentMatchState()
                    navController.popBackStack("playoff_bracket", false)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
            ) {
                Text("← НАЗАД К СЕТКЕ ПЛЕЙ-ОФФ", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PlayoffTeamScoreDisplay(
    team: com.example.cs2battlegame.model.Team,
    score: Int,
    isAttacking: Boolean,
    isSelected: Boolean,
    onTeamClick: () -> Unit
) {
    val backgroundColor = if (isAttacking) {
        Color(0x33FF9800)
    } else {
        Color(0x332196F3)
    }

    val borderColor = if (isSelected) Color(0xFF00FF88) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onTeamClick)
            .padding(8.dp)
    ) {
        SafeTeamLogo(
            teamName = team.name,
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = team.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Text(
            text = score.toString(),
            color = if (isAttacking) Color(0xFFFF9800) else Color(0xFF2196F3),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (isAttacking) "T сторона" else "CT сторона",
            color = if (isAttacking) Color(0xFFFF9800) else Color(0xFF2196F3),
            fontSize = 10.sp
        )
    }
}

@Composable
fun PlayoffPlayersStatistics(battle: com.example.cs2battlegame.model.Battle) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "СТАТИСТИКА ИГРОКОВ",
            color = Color(0xFF00FF88),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PlayoffPlayerStatsTable(
            team = battle.team1,
            players = battle.team1Players,
            isCTSide = !battle.isTeam1Attacking,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        PlayoffPlayerStatsTable(
            team = battle.team2,
            players = battle.team2Players,
            isCTSide = battle.isTeam1Attacking,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PlayoffPlayerStatsTable(
    team: com.example.cs2battlegame.model.Team,
    players: List<com.example.cs2battlegame.model.Player>,
    isCTSide: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isCTSide) {
        Color(0x332196F3)
    } else {
        Color(0x33FF9800)
    }

    Card(
        modifier = modifier,
        backgroundColor = backgroundColor,
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = team.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(3f)
                )
                Text("K", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("A", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("D", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            players.forEachIndexed { index, player ->
                PlayoffPlayerStatsRow(player = player, modifier = Modifier.fillMaxWidth())
                if (index < players.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun PlayoffPlayerStatsRow(
    player: com.example.cs2battlegame.model.Player,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = player.name,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(3f)
        )
        Text(
            text = player.kills.toString(),
            color = Color(0xFF00FF88),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = player.assists.toString(),
            color = Color(0xFFFFD700),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = player.deaths.toString(),
            color = Color(0xFFFF4444),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PlayoffBattleControls(
    battle: com.example.cs2battlegame.model.Battle,
    onSimulateRound: () -> Unit,
    onSimulateSeries: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSimulateRound,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FF88))
        ) {
            Text("🎯 СИМУЛИРОВАТЬ ОДИН РАУНД", fontSize = 16.sp)
        }

        Button(
            onClick = onSimulateSeries,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
        ) {
            Text("⚡ СИМУЛИРОВАТЬ ВСЮ СЕРИЮ", fontSize = 16.sp)
        }
    }
}

@Composable
fun PlayoffSeriesFinishedControls(
    navController: NavHostController,
    gameViewModel: GameViewModel,
    battle: com.example.cs2battlegame.model.Battle,
    playoffMatch: com.example.cs2battlegame.model.PlayoffMatch
) {
    val playerTeam = gameViewModel.gameState.value.selectedTeam
    // ИСПРАВЛЕННАЯ ЛОГИКА: Правильно определяем, выиграл ли игрок
    val playerWon = when {
        playerTeam == battle.team1 -> battle.team1Maps > battle.team2Maps
        playerTeam == battle.team2 -> battle.team2Maps > battle.team1Maps
        else -> false
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🎉 Матч плей-офф завершен!",
            color = Color(0xFF00FF88),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Финальный счет: ${battle.team1Maps}:${battle.team2Maps}",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = if (playerWon) "✅ Вы прошли дальше!" else "❌ Вы выбыли из турнира",
            color = if (playerWon) Color(0xFF00FF88) else Color(0xFFFF4444),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Показываем победителя матча для наглядности
        Text(
            text = "Победитель: ${playoffMatch.winner?.name ?: "Не определен"}",
            color = Color(0xFFFFD700),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Button(
            onClick = {
                // Вызываем completePlayoffMatch только при нажатии кнопки
                gameViewModel.completePlayoffMatch(
                    battle.team1Maps,
                    battle.team2Maps,
                    emptyList()
                )
                gameViewModel.saveCurrentMatchState()
                navController.navigate("playoff_bracket") {
                    popUpTo("playoff_bracket") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFD700))
        ) {
            Text("📊 ВЕРНУТЬСЯ К СЕТКЕ ПЛЕЙ-ОФФ", fontSize = 16.sp)
        }

        if (!playerWon) {
            Text(
                text = "Турнир будет автоматически доигран до конца",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun getPlayoffStage(matchId: String): String {
    return when {
        matchId.contains("QF") -> "Четвертьфинал"
        matchId.contains("SF") -> "Полуфинал"
        matchId.contains("Final") -> "Финал"
        else -> "Плей-офф"
    }
}