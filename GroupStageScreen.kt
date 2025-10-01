package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
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
fun GroupStageScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val tournament by gameViewModel.selectedTournament.collectAsState()
    val teams = gameViewModel.teams.value
    val playerGroupMatches by gameViewModel.playerGroupMatches.collectAsState()
    val nextMatch = playerGroupMatches.find { !it.isPlayed }
    val playoffBracket by gameViewModel.playoffBracket.collectAsState()
    val selectedTeam = gameViewModel.gameState.value.selectedTeam
    val currentBattle by gameViewModel.gameState.collectAsState()
    val hasUnfinishedGroupMatch by gameViewModel.hasUnfinishedGroupMatch.collectAsState()

    // Состояние для сворачивания/разворачивания секции "Ваши матчи"
    var isPlayerMatchesExpanded by remember { mutableStateOf(true) }

    // Восстанавливаем состояние при входе на экран
    LaunchedEffect(Unit) {
        gameViewModel.restoreMatchState()

        tournament?.let { nonNullTournament ->
            if (nonNullTournament.groups.isEmpty()) {
                gameViewModel.initializeTournament(nonNullTournament, teams)
            }
        }
        gameViewModel.initializePlayerTournament()

        // Сбрасываем текущий матч при входе в групповой этап
        gameViewModel.resetCurrentMatch()
    }

    // Сохраняем состояние при выходе с экрана
    DisposableEffect(Unit) {
        onDispose {
            gameViewModel.saveCurrentMatchState()
        }
    }

    // Если плей-офф инициализирован, переходим к сетке
    LaunchedEffect(playoffBracket) {
        if (playoffBracket != null) {
            // Сохраняем состояние перед переходом
            gameViewModel.saveCurrentMatchState()
            navController.navigate("playoff_bracket")
        }
    }

    // Проверяем, есть ли незавершенный матч плей-офф
    val hasUnfinishedPlayoffMatch = currentBattle.currentBattle != null &&
            playoffBracket != null &&
            currentBattle.currentBattle?.isSeriesFinished == false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "📊 ГРУППОВОЙ ЭТАП - ${tournament?.name ?: "Турнир"}",
            color = Color(0xFF00FF88),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Статистика турнира
        TournamentStatsCard(tournament)

        // Управление матчами игрока
        PlayerGroupControls(
            gameViewModel = gameViewModel,
            navController = navController,
            nextMatch = nextMatch,
            playerGroupMatches = playerGroupMatches,
            selectedTeam = selectedTeam,
            hasUnfinishedPlayoffMatch = hasUnfinishedPlayoffMatch,
            hasUnfinishedGroupMatch = hasUnfinishedGroupMatch,
            isPlayerMatchesExpanded = isPlayerMatchesExpanded,
            onPlayerMatchesExpandedChange = { isPlayerMatchesExpanded = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Группы
        tournament?.let { nonNullTournament ->
            if (nonNullTournament.groups.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(nonNullTournament.groups) { group ->
                        GroupTable(group = group, gameViewModel = gameViewModel)
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
                        text = "Группы формируются...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Турнир не выбран",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка назад
        Button(
            onClick = {
                gameViewModel.saveCurrentMatchState()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
        ) {
            Text("← НАЗАД К ОБЗОРУ ТУРНИРА", fontSize = 16.sp)
        }
    }
}

@Composable
fun PlayerGroupControls(
    gameViewModel: GameViewModel,
    navController: NavHostController,
    nextMatch: com.example.cs2battlegame.model.PlayerMatch?,
    playerGroupMatches: List<com.example.cs2battlegame.model.PlayerMatch>,
    selectedTeam: com.example.cs2battlegame.model.Team?,
    hasUnfinishedPlayoffMatch: Boolean = false,
    hasUnfinishedGroupMatch: Boolean = false,
    isPlayerMatchesExpanded: Boolean = true,
    onPlayerMatchesExpandedChange: (Boolean) -> Unit
) {
    val playedMatches = playerGroupMatches.count { it.isPlayed }
    val totalMatches = playerGroupMatches.size
    val canPlayFinal = gameViewModel.canPlayFinal()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Кнопка для продолжения незавершенного матча плей-офф
        if (hasUnfinishedPlayoffMatch) {
            Button(
                onClick = {
                    gameViewModel.restoreMatchState()
                    navController.navigate("playoff_battle")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFD700))
            ) {
                Text("🏆 ПРОДОЛЖИТЬ МАТЧ ПЛЕЙ-ОФФ", fontSize = 16.sp)
            }
        }

        // Кнопка для продолжения незавершенного группового матча
        if (hasUnfinishedGroupMatch) {
            Button(
                onClick = {
                    gameViewModel.restoreMatchState()
                    navController.navigate("battle")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
            ) {
                Text("🎮 ПРОДОЛЖИТЬ МАТЧ ГРУППЫ", fontSize = 16.sp)
            }
        }

        // ВЕРНУЛ КНОПКУ "ИГРАТЬ СЛЕДУЮЩИЙ МАТЧ" - ОНА ДОЛЖНА БЫТЬ ВСЕГДА ВИДНА
        if (nextMatch != null) {
            Button(
                onClick = {
                    // Сбрасываем текущий матч перед началом нового
                    gameViewModel.resetCurrentMatch()
                    val hasNext = gameViewModel.startNextPlayerMatch()
                    if (hasNext) {
                        gameViewModel.saveCurrentMatchState()
                        navController.navigate("battle")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FF88))
            ) {
                Text("🎮 ИГРАТЬ СЛЕДУЮЩИЙ МАТЧ", fontSize = 16.sp)

                // Показываем противника
                nextMatch.opponent?.let { opponent ->
                    Text(
                        text = " vs ${opponent.name}",
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Кнопка для финала (если доступна)
        if (canPlayFinal) {
            Button(
                onClick = {
                    gameViewModel.startFinalMatch()
                    gameViewModel.saveCurrentMatchState()
                    navController.navigate("playoff_battle")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFD700))
            ) {
                Text("🏆 ИГРАТЬ ФИНАЛ ТУРНИРА", fontSize = 16.sp)
            }
        }

        if (playerGroupMatches.isNotEmpty() && playedMatches == totalMatches && !canPlayFinal) {
            Text(
                text = "✅ Все матчи группы сыграны",
                color = Color(0xFF00FF88),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Автоматически переходим к плей-офф
            LaunchedEffect(Unit) {
                gameViewModel.initializePlayoffBracket()
            }
        }

        // Прогресс матчей игрока - УЛУЧШЕННАЯ ВЕРСИЯ С ВОЗМОЖНОСТЬЮ СВОРАЧИВАНИЯ
        CollapsiblePlayerMatchesProgress(
            playerGroupMatches = playerGroupMatches,
            playedMatches = playedMatches,
            totalMatches = totalMatches,
            isExpanded = isPlayerMatchesExpanded,
            onExpandedChange = onPlayerMatchesExpandedChange
        )
    }
}

@Composable
fun CollapsiblePlayerMatchesProgress(
    playerGroupMatches: List<com.example.cs2battlegame.model.PlayerMatch>,
    playedMatches: Int,
    totalMatches: Int,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF2D2D2D)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Заголовок с кнопкой сворачивания
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!isExpanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ВАШИ МАТЧИ: $playedMatches/$totalMatches",
                    color = Color(0xFF00FF88),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Иконка сворачивания/разворачивания
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    color = Color(0xFF00FF88),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = if (totalMatches > 0) playedMatches.toFloat() / totalMatches else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF00FF88),
                    backgroundColor = Color(0xFF444444)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ДЕТАЛЬНОЕ ОТОБРАЖЕНИЕ КАЖДОГО МАТЧА С РЕЗУЛЬТАТАМИ
                playerGroupMatches.forEach { match ->
                    DetailedMatchStatusItem(match = match)
                }
            } else {
                // Свернутое состояние - показываем только краткую информацию
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Нажмите для просмотра деталей",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun DetailedMatchStatusItem(match: com.example.cs2battlegame.model.PlayerMatch) {
    val backgroundColor = when {
        match.isPlayed && match.playerScore > match.opponentScore -> Color(0xFF1B5E20) // Победа - зеленый
        match.isPlayed && match.playerScore < match.opponentScore -> Color(0xFF7B1FA2) // Поражение - фиолетовый
        match.isPlayed -> Color(0xFF37474F) // Ничья (редко) - серый
        else -> Color(0xFF424242) // Не сыгран - темно-серый
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        backgroundColor = backgroundColor,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Основная информация о матче
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = "vs ${match.opponent.name}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Сила противника: ${match.opponent.getDisplayStrength()}",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                if (match.isPlayed) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${match.playerScore}:${match.opponentScore}",
                            color = if (match.playerScore > match.opponentScore) Color(0xFF00FF88) else Color(0xFFFF4444),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (match.playerScore > match.opponentScore) "ПОБЕДА"
                            else if (match.playerScore < match.opponentScore) "ПОРАЖЕНИЕ"
                            else "НИЧЬЯ",
                            color = if (match.playerScore > match.opponentScore) Color(0xFF00FF88)
                            else if (match.playerScore < match.opponentScore) Color(0xFFFF4444)
                            else Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "ОЖИДАЕТСЯ",
                        color = Color(0xFFFF9800),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Детальная информация о картах (если матч сыгран)
            if (match.isPlayed && match.mapResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Результаты карт:",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                match.mapResults.forEach { mapResult ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = mapResult.mapName,
                            color = Color.Gray,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${mapResult.team1Score}:${mapResult.team2Score}",
                            color = if (mapResult.winner == match.opponent) Color(0xFFFF4444) else Color(0xFF00FF88),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Статистика матча
            if (match.isPlayed) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Общий счет раундов: ${calculateTotalRounds(match.mapResults)}",
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// Вспомогательная функция для подсчета общего счета раундов
private fun calculateTotalRounds(mapResults: List<com.example.cs2battlegame.model.MapResult>): String {
    val team1Total = mapResults.sumBy { it.team1Score }
    val team2Total = mapResults.sumBy { it.team2Score }
    return "$team1Total:$team2Total"
}

@Composable
fun TournamentStatsCard(tournament: com.example.cs2battlegame.model.Tournament?) {
    val playedMatches = tournament?.groups?.flatMap { it.matches }?.count { it.isPlayed } ?: 0
    val totalMatches = tournament?.groups?.flatMap { it.matches }?.size ?: 0
    val progress = if (totalMatches > 0) playedMatches.toFloat() / totalMatches else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF2D2D2D),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ПРОГРЕСС ГРУППОВОГО ЭТАПА",
                color = Color(0xFF00FF88),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Сыграно матчей: $playedMatches/$totalMatches",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF00FF88),
                backgroundColor = Color(0xFF444444)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                TournamentStatItem("Группы", "${tournament?.groups?.size ?: 0}", Modifier.weight(1f))
                TournamentStatItem("Команд", "${tournament?.participantCount ?: 0}", Modifier.weight(1f))
                TournamentStatItem("Прогресс", "${(progress * 100).toInt()}%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RowScope.TournamentStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color(0xFF00FF88),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun GroupTable(group: com.example.cs2battlegame.model.TournamentGroup, gameViewModel: GameViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF2D2D2D),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${group.matches.count { it.isPlayed }}/${group.matches.size} матчей",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Заголовок таблицы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Команда", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(3f))
                Text("В", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("П", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("РР", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("О", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Строки команд
            group.teams.sortedByDescending { it.points }.forEachIndexed { index, groupTeam ->
                GroupTableRow(
                    groupTeam = groupTeam,
                    position = index + 1,
                    gameViewModel = gameViewModel
                )
                if (index < group.teams.size - 1) {
                    Divider(color = Color(0xFF444444), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Последние матчи группы
            Text(
                text = "Последние матчи:",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val recentMatches = group.matches.filter { it.isPlayed }.takeLast(3)
            if (recentMatches.isNotEmpty()) {
                recentMatches.forEach { match ->
                    MatchResultRow(match = match)
                }
            } else {
                Text(
                    text = "Матчи еще не сыграны",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun GroupTableRow(
    groupTeam: com.example.cs2battlegame.model.GroupTeam,
    position: Int,
    gameViewModel: GameViewModel
) {
    val isQualified = position <= 2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { gameViewModel.showTeamStats(groupTeam.team) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(3f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position.",
                color = if (isQualified) Color(0xFF00FF88) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )

            Text(
                text = groupTeam.team.name,
                color = if (isQualified) Color(0xFF00FF88) else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isQualified) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )

            if (isQualified) {
                Text(
                    text = " 🏆",
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "${groupTeam.wins}",
            color = Color(0xFF00FF88),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${groupTeam.losses}",
            color = Color(0xFFFF4444),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${groupTeam.roundDifference}",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${groupTeam.points}",
            color = Color(0xFFFFD700),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MatchResultRow(match: com.example.cs2battlegame.model.GroupMatch) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = match.team1.name,
            color = if (match.winner == match.team1) Color(0xFF00FF88) else Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )

        Text(
            text = "${match.team1Score}:${match.team2Score}",
            color = Color(0xFFFFD700),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = match.team2.name,
            color = if (match.winner == match.team2) Color(0xFF00FF88) else Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
    }
}