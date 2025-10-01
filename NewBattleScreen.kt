package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.utils.ColorUtils
import com.example.cs2battlegame.utils.TeamLogo
import com.example.cs2battlegame.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NewBattleScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val gameState by gameViewModel.gameState.collectAsState()
    val battle by remember { derivedStateOf { gameState.currentBattle } }
    val selectedTeam by remember { derivedStateOf { gameState.selectedTeam } }
    val gameSettings by gameViewModel.gameSettings.collectAsState()
    val isTournamentMatch by gameViewModel.isTournamentMatch.collectAsState()
    val completedBattle by gameViewModel.completedBattle.collectAsState()

    var currentRoundResult by remember { mutableStateOf<com.example.cs2battlegame.model.RoundResult?>(null) }
    var forceUpdate by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) }
    var isSimulatingFullMatch by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val displayBattle = battle ?: completedBattle

    // Автосимуляция
    LaunchedEffect(gameSettings.autoPlayEnabled, battle) {
        if (gameSettings.autoPlayEnabled && battle != null && !battle!!.isSeriesFinished) {
            while (gameSettings.autoPlayEnabled && battle != null && !battle!!.isSeriesFinished) {
                delay(gameSettings.roundDurationMs.toLong())
                val result = battle!!.simulateRound()
                currentRoundResult = result
                forceUpdate++

                if (result == com.example.cs2battlegame.model.RoundResult.SERIES_FINISHED) {
                    break
                }
            }
        }
    }

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

    if (displayBattle == null || selectedTeam == null) {
        BattleErrorScreen(navController, isTournamentMatch)
    } else {
        ScrollableBattleInterface(
            battle = displayBattle,
            selectedTeam = selectedTeam!!,
            gameViewModel = gameViewModel,
            navController = navController,
            currentRoundResult = currentRoundResult,
            forceUpdate = forceUpdate,
            gameSettings = gameSettings,
            isTournamentMatch = isTournamentMatch,
            isCompletedBattle = completedBattle != null,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            isSimulatingFullMatch = isSimulatingFullMatch,
            onSimulateFullMatch = {
                isSimulatingFullMatch = true
                coroutineScope.launch {
                    // Запускаем симуляцию в фоновом потоке
                    displayBattle.simulateFullSeries()
                    // После завершения симуляции обновляем состояние
                    isSimulatingFullMatch = false
                    forceUpdate++
                    gameViewModel.saveCurrentMatchState()
                }
            }
        )
    }
}

@Composable
fun ScrollableBattleInterface(
    battle: com.example.cs2battlegame.model.Battle,
    selectedTeam: com.example.cs2battlegame.model.Team,
    gameViewModel: GameViewModel,
    navController: NavHostController,
    currentRoundResult: com.example.cs2battlegame.model.RoundResult?,
    forceUpdate: Int,
    gameSettings: com.example.cs2battlegame.model.GameSettings,
    isTournamentMatch: Boolean,
    isCompletedBattle: Boolean,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isSimulatingFullMatch: Boolean,
    onSimulateFullMatch: () -> Unit
) {
    val isSeriesFinished = battle.isSeriesFinished || isCompletedBattle

    // Модификатор для вертикальной прокрутки всего экрана
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(scrollState) // Добавляем вертикальную прокрутку
    ) {
        // Верхняя часть с CS2 лого и названиями команд
        BattleHeaderSection(
            battle = battle,
            selectedTeam = selectedTeam,
            navController = navController,
            gameViewModel = gameViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        )

        // Основной блок с командами и счетом
        TeamScoreSection(
            battle = battle,
            selectedTeam = selectedTeam,
            gameViewModel = gameViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        // Центральная часть с информацией о карте
        MapInfoSection(
            battle = battle,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        // Вкладки
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFF1A1A1A),
            contentColor = Color.White
        ) {
            Tab(
                text = { Text("SUMMARY") },
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                selectedContentColor = Color(0xFF00FF88),
                unselectedContentColor = Color.White
            )
            Tab(
                text = { Text("PLAY BY PLAY") },
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                selectedContentColor = Color(0xFF00FF88),
                unselectedContentColor = Color.White
            )
            Tab(
                text = { Text("STATS") },
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                selectedContentColor = Color(0xFF00FF88),
                unselectedContentColor = Color.White
            )
        }

        // Содержимое вкладок
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            when (selectedTab) {
                0 -> SummaryTabContent(
                    battle = battle,
                    gameSettings = gameSettings,
                    isSeriesFinished = isSeriesFinished,
                    isCompletedBattle = isCompletedBattle,
                    onSimulateFullMatch = onSimulateFullMatch,
                    onToggleAutoPlay = {
                        gameViewModel.toggleAutoPlay()
                    },
                    navController = navController,
                    gameViewModel = gameViewModel,
                    isTournamentMatch = isTournamentMatch,
                    isSimulatingFullMatch = isSimulatingFullMatch,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> PlayByPlayTabContent(
                    battle = battle,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> StatsTabContent(
                    battle = battle,
                    gameViewModel = gameViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun BattleHeaderSection(
    battle: com.example.cs2battlegame.model.Battle,
    selectedTeam: com.example.cs2battlegame.model.Team,
    navController: NavHostController,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Логотип CS2 для возврата в меню
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    gameViewModel.saveCurrentMatchState()
                    navController.popBackStack("main_menu", false)
                }
                .background(Color(0xFF00FF88), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CS2",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Названия команд и формат
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${battle.team1.name} vs ${battle.team2.name}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "BO3 SERIES",
                color = Color(0xFF00FF88),
                fontSize = 12.sp
            )
        }

        // Пустой элемент для баланса
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
fun TeamScoreSection(
    battle: com.example.cs2battlegame.model.Battle,
    selectedTeam: com.example.cs2battlegame.model.Team,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Левая команда
        TeamScoreDisplay(
            team = battle.team1,
            mapsWon = battle.team1Maps,
            currentScore = battle.team1Score,
            isSelected = battle.team1 == selectedTeam,
            onTeamClick = { gameViewModel.showTeamStats(battle.team1) },
            modifier = Modifier.weight(1f)
        )

        // Центральный разделитель
        Text(
            text = "VS",
            color = Color(0xFFFFD700),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Правая команда
        TeamScoreDisplay(
            team = battle.team2,
            mapsWon = battle.team2Maps,
            currentScore = battle.team2Score,
            isSelected = battle.team2 == selectedTeam,
            onTeamClick = { gameViewModel.showTeamStats(battle.team2) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TeamScoreDisplay(
    team: com.example.cs2battlegame.model.Team,
    mapsWon: Int,
    currentScore: Int,
    isSelected: Boolean,
    onTeamClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val teamColor = ColorUtils.getTeamPrimaryColor(team.name)
    val scoreBackgroundColor = ColorUtils.getTeamScoreBackgroundColor(team.name)

    Column(
        modifier = modifier
            .clickable(onClick = onTeamClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Логотип команды на фоне фирменного цвета
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(teamColor),
            contentAlignment = Alignment.Center
        ) {
            TeamLogo(
                teamName = team.name,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Счет по картам (Maps)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(8.dp))
                .background(teamColor)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mapsWon.toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "MAPS",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Счет текущих раундов
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(scoreBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentScore.toString(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Выделение выбранной команды
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF00FF88))
            )
        }
    }
}

@Composable
fun MapInfoSection(
    battle: com.example.cs2battlegame.model.Battle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = battle.getCurrentMapName().uppercase(),
            color = Color(0xFFFFD700),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SummaryTabContent(
    battle: com.example.cs2battlegame.model.Battle,
    gameSettings: com.example.cs2battlegame.model.GameSettings,
    isSeriesFinished: Boolean,
    isCompletedBattle: Boolean,
    onSimulateFullMatch: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    navController: NavHostController,
    gameViewModel: GameViewModel,
    isTournamentMatch: Boolean,
    isSimulatingFullMatch: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0A0A0A))
            .padding(16.dp)
    ) {
        // Информация о командах и картах
        TeamsAndMapsInfo(
            battle = battle,
            gameViewModel = gameViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки управления
        if (!isSeriesFinished && !isCompletedBattle) {
            BattleControls(
                battle = battle,
                gameSettings = gameSettings,
                onSimulateFullMatch = onSimulateFullMatch,
                onToggleAutoPlay = onToggleAutoPlay,
                navController = navController,
                gameViewModel = gameViewModel,
                isSimulatingFullMatch = isSimulatingFullMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            )
        } else {
            SeriesResult(
                battle = battle,
                navController = navController,
                gameViewModel = gameViewModel,
                isTournamentMatch = isTournamentMatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            )
        }
    }
}

@Composable
fun TeamsAndMapsInfo(
    battle: com.example.cs2battlegame.model.Battle,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Заголовок
        Text(
            text = "MATCH INFORMATION",
            color = Color(0xFF00FF88),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Информация о командах
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Команда 1
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                TeamLogo(
                    teamName = battle.team1.name,
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = battle.team1.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = battle.team1.country,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            // Команда 2
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                TeamLogo(
                    teamName = battle.team2.name,
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = battle.team2.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = battle.team2.country,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mappool
        Text(
            text = "MAPPOOL:",
            color = Color(0xFF00FF88),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            battle.selectedMaps.forEachIndexed { index, map ->
                val isCurrentMap = (index + 1) == battle.currentMap
                val backgroundColor = if (isCurrentMap) Color(0xFFFFD700) else Color(0xFF333333)
                val textColor = if (isCurrentMap) Color.Black else Color.White

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = map.getDisplayName(),
                        color = textColor,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrentMap) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun BattleControls(
    battle: com.example.cs2battlegame.model.Battle,
    gameSettings: com.example.cs2battlegame.model.GameSettings,
    onSimulateFullMatch: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    navController: NavHostController,
    gameViewModel: GameViewModel,
    isSimulatingFullMatch: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // === ИСПРАВЛЕННАЯ КНОПКА: Теперь сразу симулирует весь матч ===
        Button(
            onClick = onSimulateFullMatch,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSimulatingFullMatch,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isSimulatingFullMatch) Color(0xFF666666) else Color(0xFF00FF88)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚡", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = if (isSimulatingFullMatch) "СИМУЛЯЦИЯ..." else "СИМУЛИРОВАТЬ ВЕСЬ МАТЧ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Кнопка автосимуляции
        Button(
            onClick = onToggleAutoPlay,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSimulatingFullMatch,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isSimulatingFullMatch) Color(0xFF666666) else if (gameSettings.autoPlayEnabled) Color(0xFFFF9800) else Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔄", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = if (gameSettings.autoPlayEnabled) "ОСТАНОВИТЬ АВТОСИМУЛЯЦИЮ"
                    else "ЗАПУСТИТЬ АВТОСИМУЛЯЦИЮ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Кнопка настроек
        Button(
            onClick = {
                gameViewModel.openSettingsFromBattle(navController)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSimulatingFullMatch,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isSimulatingFullMatch) Color(0xFF666666) else Color(0xFF666666)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚙", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                Text("НАСТРОЙКИ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (gameSettings.autoPlayEnabled) {
            Text(
                text = "Интервал: ${gameSettings.roundDurationMs}мс",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (isSimulatingFullMatch) {
            Text(
                text = "Идет симуляция матча...",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun PlayByPlayTabContent(
    battle: com.example.cs2battlegame.model.Battle,
    modifier: Modifier = Modifier
) {
    // Определяем команды на сторонах T и CT
    val (tTeam, ctTeam) = if (battle.isFirstHalf) {
        if (battle.isTeam1Attacking) battle.team1 to battle.team2 else battle.team2 to battle.team1
    } else {
        if (battle.isTeam1Attacking) battle.team2 to battle.team1 else battle.team1 to battle.team2
    }

    val tScore = if (tTeam == battle.team1) battle.team1Score else battle.team2Score
    val ctScore = if (ctTeam == battle.team1) battle.team1Score else battle.team2Score

    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
    ) {
        // Заголовок с информацией о раунде
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Round ${battle.round} of 24",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Основной контейнер с командами
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2D2D2D))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Левая колонка - T сторона (оранжевая)
            TeamColumn(
                team = tTeam,
                score = tScore,
                side = "T",
                sideColor = Color(0xFFFF9800),
                players = if (tTeam == battle.team1) battle.team1Players else battle.team2Players,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            // Правая колонка - CT сторона (голубая)
            TeamColumn(
                team = ctTeam,
                score = ctScore,
                side = "CT",
                sideColor = Color(0xFF2196F3),
                players = if (ctTeam == battle.team1) battle.team1Players else battle.team2Players,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        // Лог матча внизу
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFF1A1A1A))
                .padding(top = 16.dp)
        ) {
            Text(
                text = "MATCH LOG",
                color = Color(0xFF00FF88),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(battle.battleLog.reversed()) { logEntry ->
                    Text(
                        text = logEntry,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TeamColumn(
    team: com.example.cs2battlegame.model.Team,
    score: Int,
    side: String,
    sideColor: Color,
    players: List<com.example.cs2battlegame.model.Player>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF333333))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок команды
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = team.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = side,
                    color = sideColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = score.toString(),
                color = sideColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Логотип команды
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF444444))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            TeamLogo(
                teamName = team.name,
                modifier = Modifier.size(60.dp)
            )
        }

        // Список игроков
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            players.forEach { player ->
                PlayerRow(
                    player = player,
                    sideColor = sideColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PlayerRow(
    player: com.example.cs2battlegame.model.Player,
    sideColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        backgroundColor = Color(0xFF444444),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Имя игрока и здоровье
            Column(
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                // Полоска здоровья
                HealthBar(
                    health = player.health,
                    color = sideColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .padding(top = 4.dp)
                )
            }

            // Статистика K/D/A
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${player.kills}",
                    color = Color(0xFF00FF88),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${player.deaths}",
                    color = Color(0xFFFF4444),
                    fontSize = 12.sp
                )
                Text(
                    text = "${player.assists}",
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun HealthBar(
    health: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF666666), RoundedCornerShape(3.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(health / 100f)
                .background(color, RoundedCornerShape(3.dp))
                .height(6.dp)
        )
    }
}


@Composable
fun StatsTabContent(
    battle: com.example.cs2battlegame.model.Battle,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0A0A0A))
            .padding(16.dp)
    ) {
        Text(
            text = "PLAYER STATISTICS",
            color = Color(0xFF00FF88),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Статистика команды 1
        Text(
            text = battle.team1.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        battle.team1Players.forEach { player ->
            PlayerStatsRow(player = player, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Статистика команды 2
        Text(
            text = battle.team2.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        battle.team2Players.forEach { player ->
            PlayerStatsRow(player = player, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun PlayerStatsRow(
    player: com.example.cs2battlegame.model.Player,
    modifier: Modifier = Modifier
) {
    val kdRatio = if (player.deaths > 0) player.kills.toFloat() / player.deaths else player.kills.toFloat()

    Row(
        modifier = modifier
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = player.name,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(3f)
        )

        Text(
            text = "${player.kills}",
            color = Color(0xFF00FF88),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${player.deaths}",
            color = Color(0xFFFF4444),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${player.assists}",
            color = Color(0xFFFFD700),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "%.2f".format(kdRatio),
            color = if (kdRatio >= 1.0) Color(0xFF00FF88) else Color(0xFFFF4444),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SeriesResult(
    battle: com.example.cs2battlegame.model.Battle,
    navController: NavHostController,
    gameViewModel: GameViewModel,
    isTournamentMatch: Boolean,
    modifier: Modifier = Modifier
) {
    val winner = if (battle.team1Maps > battle.team2Maps) battle.team1 else battle.team2

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏆 СЕРИЯ ЗАВЕРШЕНА",
            color = Color(0xFFFFD700),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ПОБЕДИТЕЛЬ: ${winner.name}",
            color = Color(0xFF00FF88),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "ФИНАЛЬНЫЙ СЧЕТ: ${battle.team1Maps}:${battle.team2Maps}",
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                gameViewModel.saveCurrentMatchState()
                if (isTournamentMatch) {
                    navController.navigate("group_stage") {
                        popUpTo("battle") { inclusive = true }
                    }
                } else {
                    navController.popBackStack("main_menu", false)
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
        ) {
            Text(
                if (isTournamentMatch) "← В ГРУППОВОЙ ЭТАП" else "← В ГЛАВНОЕ МЕНЮ",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun BattleErrorScreen(navController: NavHostController, isTournamentMatch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌ МАТЧ НЕ НАЧАТ",
            color = Color.Red,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isTournamentMatch) {
                    navController.navigate("group_stage")
                } else {
                    navController.popBackStack("main_menu", false)
                }
            }
        ) {
            Text(
                if (isTournamentMatch) "Вернуться в групповой этап"
                else "Вернуться в главное меню"
            )
        }
    }
}