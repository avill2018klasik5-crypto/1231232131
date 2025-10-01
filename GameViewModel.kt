package com.example.cs2battlegame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs2battlegame.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private val _currentPlayerMatch = MutableStateFlow<PlayerMatch?>(null)
    val currentPlayerMatch: StateFlow<PlayerMatch?> = _currentPlayerMatch.asStateFlow()

    private val _playerGroupMatches = MutableStateFlow<List<PlayerMatch>>(emptyList())
    val playerGroupMatches: StateFlow<List<PlayerMatch>> = _playerGroupMatches.asStateFlow()

    private val _playoffBracket = MutableStateFlow<PlayoffBracket?>(null)
    val playoffBracket: StateFlow<PlayoffBracket?> = _playoffBracket.asStateFlow()

    private val _currentPlayoffMatch = MutableStateFlow<PlayoffMatch?>(null)
    val currentPlayoffMatch: StateFlow<PlayoffMatch?> = _currentPlayoffMatch.asStateFlow()

    // === ОСНОВНЫЕ ПЕРЕМЕННЫЕ ===
    private val _gameState = MutableStateFlow(Game())
    val gameState: StateFlow<Game> = _gameState.asStateFlow()

    private val _teams = MutableStateFlow(createDefaultTeams())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    private val _selectedTeamForStats = MutableStateFlow<Team?>(null)
    val selectedTeamForStats: StateFlow<Team?> = _selectedTeamForStats.asStateFlow()

    private val _gameSettings = MutableStateFlow(GameSettings())
    val gameSettings: StateFlow<GameSettings> = _gameSettings.asStateFlow()

    private val _selectedTournament = MutableStateFlow<Tournament?>(null)
    val selectedTournament: StateFlow<Tournament?> = _selectedTournament.asStateFlow()

    private var autoPlayJob: Job? = null

    private val availableMaps = listOf(
        MapPool.MIRAGE, MapPool.INFERNO, MapPool.DUST2, MapPool.OVERPASS,
        MapPool.VERTIGO, MapPool.ANCIENT, MapPool.NUKE, MapPool.ANUBIS, MapPool.TRAIN
    )

    // Временное хранилище состояний
    private var savedBattleState: Battle? = null
    private var savedPlayoffMatchState: PlayoffMatch? = null
    private var savedGroupMatchesState: List<PlayerMatch> = emptyList()
    private var savedTournamentState: Tournament? = null

    // Флаг для отслеживания незавершенного группового матча
    private val _hasUnfinishedGroupMatch = MutableStateFlow(false)
    val hasUnfinishedGroupMatch: StateFlow<Boolean> = _hasUnfinishedGroupMatch.asStateFlow()

    // Флаг для определения типа текущего матча (турнирный или обычный)
    private val _isTournamentMatch = MutableStateFlow(false)
    val isTournamentMatch: StateFlow<Boolean> = _isTournamentMatch.asStateFlow()

    // Флаг для отслеживания завершенной битвы (чтобы показывать результаты)
    private val _completedBattle = MutableStateFlow<Battle?>(null)
    val completedBattle: StateFlow<Battle?> = _completedBattle.asStateFlow()

    init {
        viewModelScope.launch {
            updateTeamsStrength()
        }
    }

    // === НОВАЯ ФУНКЦИЯ: Установка завершенной битвы ===
    fun setCompletedBattle(battle: Battle?) {
        _completedBattle.value = battle
    }

    // === УДАЛЕНА ФУНКЦИЯ getLimitedTeams() - ТЕПЕРЬ ИСПОЛЬЗУЕМ ВСЕ 32 КОМАНДЫ ===

    // === НОВАЯ ФУНКЦИЯ: Проверка незавершенного группового матча ===
    fun checkUnfinishedGroupMatch(): Boolean {
        val currentBattle = _gameState.value.currentBattle
        val hasGroupMatches = _playerGroupMatches.value.isNotEmpty()
        val isSeriesFinished = currentBattle?.isSeriesFinished ?: true

        val hasUnfinished = currentBattle != null &&
                hasGroupMatches &&
                !isSeriesFinished

        _hasUnfinishedGroupMatch.value = hasUnfinished
        return hasUnfinished
    }

    // === НОВАЯ ФУНКЦИЯ: Установка типа матча ===
    fun setMatchType(isTournament: Boolean) {
        _isTournamentMatch.value = isTournament
    }

    // === ОБНОВЛЕННАЯ ФУНКЦИЯ: Расчет вероятности победы на основе силы ===
    private fun calculateMatchWinProbability(team1: Team, team2: Team): Double {
        val team1Strength = team1.getDisplayStrength().toDouble()
        val team2Strength = team2.getDisplayStrength().toDouble()
        val totalStrength = team1Strength + team2Strength

        // Базовая вероятность 50%, корректируется разницей в силе
        val baseProbability = 0.5
        val strengthDifference = (team1Strength - team2Strength) / totalStrength
        val adjustedProbability = baseProbability + (strengthDifference * 0.4)

        return adjustedProbability.coerceIn(0.1, 0.9)
    }

    // === НОВАЯ ФУНКЦИЯ: Сброс текущего матча ===
    fun resetCurrentMatch() {
        _currentPlayerMatch.value = null
        _gameState.value = _gameState.value.copy(currentBattle = null)
        _currentPlayoffMatch.value = null
        _hasUnfinishedGroupMatch.value = false
        _isTournamentMatch.value = false
        _completedBattle.value = null
    }

    // === ФУНКЦИИ СОХРАНЕНИЯ СОСТОЯНИЯ ===
    fun saveCurrentMatchState() {
        val currentBattle = _gameState.value.currentBattle
        val currentPlayoffMatch = _currentPlayoffMatch.value

        if (currentBattle != null) {
            saveBattleState(currentBattle)
        }

        if (currentPlayoffMatch != null) {
            savePlayoffMatchState(currentPlayoffMatch)
        }

        // Сохраняем прогресс групповых матчей
        savedGroupMatchesState = _playerGroupMatches.value.toList()

        // Сохраняем состояние турнира
        savedTournamentState = _selectedTournament.value?.copy()

        // Обновляем статус незавершенного матча
        checkUnfinishedGroupMatch()
    }

    fun restoreMatchState() {
        val restoredBattle = restoreBattleState()
        val restoredPlayoffMatch = restorePlayoffMatchState()

        if (restoredBattle != null) {
            _gameState.value = _gameState.value.copy(currentBattle = restoredBattle)
        }

        if (restoredPlayoffMatch != null) {
            _currentPlayoffMatch.value = restoredPlayoffMatch
        }

        // Восстанавливаем прогресс групповых матчей
        if (savedGroupMatchesState.isNotEmpty()) {
            _playerGroupMatches.value = savedGroupMatchesState
        }

        // Восстанавливаем состояние турнира
        if (savedTournamentState != null) {
            _selectedTournament.value = savedTournamentState
        }

        // Обновляем статус незавершенного матча
        checkUnfinishedGroupMatch()
    }

    private fun saveBattleState(battle: Battle) {
        savedBattleState = battle.copy()
    }

    private fun savePlayoffMatchState(match: PlayoffMatch) {
        savedPlayoffMatchState = match.copy()
    }

    private fun restoreBattleState(): Battle? {
        return savedBattleState
    }

    private fun restorePlayoffMatchState(): PlayoffMatch? {
        return savedPlayoffMatchState
    }

    // === НОВАЯ ФУНКЦИЯ: Открытие настроек из битвы ===
    fun openSettingsFromBattle(navController: androidx.navigation.NavHostController) {
        saveCurrentMatchState()
        navController.navigate("settings")
    }

    // === ТУРНИРНЫЕ ФУНКЦИИ ДЛЯ ИГРОКА ===
    fun initializePlayerTournament() {
        val selectedTeam = _gameState.value.selectedTeam ?: return
        val tournament = _selectedTournament.value ?: return

        // Находим группу игрока
        val playerGroup = tournament.groups.find { group ->
            group.teams.any { it.team == selectedTeam }
        }

        playerGroup?.let { group ->
            // Создаем матчи против всех команд группы (кроме своей)
            val matches = group.teams
                .filter { it.team != selectedTeam }
                .map { team ->
                    PlayerMatch(
                        opponent = team.team,
                        maps = availableMaps.shuffled().take(3)
                    )
                }

            _playerGroupMatches.value = matches
        }

        // Сбрасываем статус незавершенного матча
        _hasUnfinishedGroupMatch.value = false
    }

    fun startNextPlayerMatch(): Boolean {
        val nextMatch = _playerGroupMatches.value.find { !it.isPlayed }
        _currentPlayerMatch.value = nextMatch

        // Автоматически создаем битву для этого матча
        nextMatch?.let { match ->
            val selectedTeam = _gameState.value.selectedTeam
            selectedTeam?.let { myTeam ->
                val selectedMaps = availableMaps.shuffled().take(3)
                val battle = Battle(
                    team1 = myTeam,
                    team2 = match.opponent,
                    selectedMaps = selectedMaps
                )
                _gameState.value = _gameState.value.copy(currentBattle = battle)

                // Устанавливаем тип матча как турнирный
                _isTournamentMatch.value = true

                // Обновляем статус незавершенного матча
                _hasUnfinishedGroupMatch.value = true

                // Сбрасываем завершенную битву
                _completedBattle.value = null
            }
        }

        return nextMatch != null
    }

    fun simulateOneRoundGroupMatches() {
        val tournament = _selectedTournament.value ?: return
        val playerTeam = _gameState.value.selectedTeam

        // Симулируем только по одному матчу в каждой группе
        tournament.groups.forEach { group ->
            val unfinishedMatches = group.matches.filter {
                it.team1 != playerTeam && it.team2 != playerTeam && !it.isPlayed
            }

            if (unfinishedMatches.isNotEmpty()) {
                // Берем только первый незавершенный матч в группе
                val matchToSimulate = unfinishedMatches.first()
                simulateGroupMatch(matchToSimulate)
            }
        }

        _selectedTournament.value = tournament.copy()
    }

    // === ОБНОВЛЕННАЯ ФУНКЦИЯ: Симуляция группового матча с учетом силы ===
    private fun simulateGroupMatch(match: GroupMatch) {
        // Симуляция BO3 матча
        val maps = availableMaps.shuffled().take(3)
        val mapResults = mutableListOf<MapResult>()

        var team1Wins = 0
        var team2Wins = 0

        // Рассчитываем вероятность победы для каждой карты
        val winProbability = calculateMatchWinProbability(match.team1, match.team2)

        // Симулируем до 3 карт
        for ((index, map) in maps.withIndex()) {
            if (team1Wins == 2 || team2Wins == 2) break // Уже есть победитель

            // Используем вероятность на основе силы для определения победителя карты
            val winner = if (Random.nextDouble() < winProbability) match.team1 else match.team2
            val winnerScore = 13
            val loserScore = Random.nextInt(0, 13)

            val team1Score = if (winner == match.team1) winnerScore else loserScore
            val team2Score = if (winner == match.team2) winnerScore else loserScore

            mapResults.add(MapResult(
                mapNumber = index + 1,
                mapName = map.getDisplayName(),
                team1Score = team1Score,
                team2Score = team2Score,
                winner = winner
            ))

            if (winner == match.team1) team1Wins++ else team2Wins++
        }

        match.team1Score = team1Wins
        match.team2Score = team2Wins
        match.mapResults = mapResults
        match.isPlayed = true
        match.winner = if (team1Wins > team2Wins) match.team1 else match.team2

        updateGroupTeamStats(match)
    }

    private fun updateGroupTeamStats(match: GroupMatch) {
        val tournament = _selectedTournament.value ?: return

        tournament.groups.forEach { group ->
            val team1InGroup = group.teams.find { it.team == match.team1 }
            val team2InGroup = group.teams.find { it.team == match.team2 }

            team1InGroup?.let {
                if (match.winner == match.team1) {
                    val roundDiff = match.mapResults.sumBy { it.team1Score - it.team2Score }
                    it.addWin(roundDiff)
                } else {
                    val roundDiff = match.mapResults.sumBy { it.team2Score - it.team1Score }
                    it.addLoss(roundDiff)
                }
            }

            team2InGroup?.let {
                if (match.winner == match.team2) {
                    val roundDiff = match.mapResults.sumBy { it.team2Score - it.team1Score }
                    it.addWin(roundDiff)
                } else {
                    val roundDiff = match.mapResults.sumBy { it.team1Score - it.team2Score }
                    it.addLoss(roundDiff)
                }
            }
        }
    }

    // === ИСПРАВЛЕННАЯ ФУНКЦИЯ: Завершение матча игрока с сохранением прогресса ===
    fun completePlayerMatch(playerMapsWon: Int, opponentMapsWon: Int, mapResults: List<MapResult>) {
        val currentMatch = _currentPlayerMatch.value ?: return
        val playerTeam = _gameState.value.selectedTeam ?: return
        val currentBattle = _gameState.value.currentBattle ?: return

        currentMatch.isPlayed = true
        currentMatch.playerScore = playerMapsWon
        currentMatch.opponentScore = opponentMapsWon
        currentMatch.mapResults = mapResults
        currentMatch.winner = if (playerMapsWon > opponentMapsWon) playerTeam else currentMatch.opponent

        // ВАЖНОЕ ИСПРАВЛЕНИЕ: Принудительно обновляем StateFlow для playerGroupMatches
        _playerGroupMatches.value = _playerGroupMatches.value.toList()

        // Обновляем турнирную таблицу
        updateGroupStandingsAfterPlayerMatch(currentMatch)

        // Симулируем только один раунд матчей в группах
        simulateOneRoundGroupMatches()

        // Сохраняем завершенную битву перед сбросом
        _completedBattle.value = currentBattle

        // Сбрасываем текущий матч, но сохраняем завершенную битву для отображения результатов
        _currentPlayerMatch.value = null
        _gameState.value = _gameState.value.copy(currentBattle = null)
        _hasUnfinishedGroupMatch.value = false

        // ВАЖНОЕ ИСПРАВЛЕНИЕ: Принудительно сохраняем состояние после завершения матча
        saveCurrentMatchState()

        // Проверяем, все ли матчи игрока сыграны
        val allPlayerMatchesPlayed = _playerGroupMatches.value.all { it.isPlayed }
        if (allPlayerMatchesPlayed) {
            // Переходим к плей-офф
            initializePlayoffBracket()
        }
    }

    private fun updateGroupStandingsAfterPlayerMatch(match: PlayerMatch) {
        val tournament = _selectedTournament.value ?: return
        val playerTeam = _gameState.value.selectedTeam ?: return

        tournament.groups.forEach { group ->
            val playerInGroup = group.teams.find { it.team == playerTeam }
            val opponentInGroup = group.teams.find { it.team == match.opponent }

            playerInGroup?.let {
                if (match.winner == playerTeam) {
                    val roundDiff = match.mapResults.sumBy {
                        if (it.winner == playerTeam) it.team1Score - it.team2Score else it.team2Score - it.team1Score
                    }
                    it.addWin(roundDiff)
                } else {
                    val roundDiff = match.mapResults.sumBy {
                        if (it.winner == playerTeam) it.team1Score - it.team2Score else it.team2Score - it.team1Score
                    }
                    it.addLoss(roundDiff)
                }
            }

            opponentInGroup?.let {
                if (match.winner == match.opponent) {
                    val roundDiff = match.mapResults.sumBy {
                        if (it.winner == playerTeam) it.team1Score - it.team2Score else it.team2Score - it.team1Score
                    }
                    it.addWin(roundDiff)
                } else {
                    val roundDiff = match.mapResults.sumBy {
                        if (it.winner == playerTeam) it.team1Score - it.team2Score else it.team2Score - it.team1Score
                    }
                    it.addLoss(roundDiff)
                }
            }
        }

        _selectedTournament.value = tournament.copy()
    }

    // === СИСТЕМА ПЛЕЙ-ОФФ ===
    fun initializePlayoffBracket() {
        val tournament = _selectedTournament.value ?: return

        // Получаем команды, вышедшие из групп (первые 2 места из каждой группы)
        val qualifiedTeams = tournament.groups.flatMap { group ->
            group.teams.sortedByDescending { it.points }.take(2)
        }.map { it.team }.shuffled()

        if (qualifiedTeams.size >= 8) {
            // Создаем четвертьфиналы
            val quarterFinals = listOf(
                PlayoffMatch("QF1", qualifiedTeams[0], qualifiedTeams[1]),
                PlayoffMatch("QF2", qualifiedTeams[2], qualifiedTeams[3]),
                PlayoffMatch("QF3", qualifiedTeams[4], qualifiedTeams[5]),
                PlayoffMatch("QF4", qualifiedTeams[6], qualifiedTeams[7])
            )

            val bracket = PlayoffBracket(quarterFinals = quarterFinals)
            _playoffBracket.value = bracket
            _selectedTournament.value = tournament.copy(
                currentStage = TournamentStage.Playoffs,
                playoffBracket = bracket
            )
        }
    }

    fun simulatePlayoffRound() {
        val bracket = _playoffBracket.value ?: return
        val selectedTeam = _gameState.value.selectedTeam ?: return

        // Находим следующий матч с участием команды игрока
        val nextPlayerMatch = findNextPlayerPlayoffMatch(bracket, selectedTeam)

        if (nextPlayerMatch != null && !nextPlayerMatch.isCompleted) {
            // Если есть незавершенный матч игрока, начинаем его
            startPlayoffMatch(nextPlayerMatch)
        }
    }

    private fun findNextPlayerPlayoffMatch(bracket: PlayoffBracket, playerTeam: Team): PlayoffMatch? {
        val allMatches = bracket.quarterFinals + bracket.semiFinals + listOfNotNull(bracket.final)
        return allMatches.firstOrNull { match ->
            !match.isCompleted && (match.team1 == playerTeam || match.team2 == playerTeam)
        }
    }

    fun startPlayoffMatch(match: PlayoffMatch) {
        val team1 = match.team1 ?: return
        val team2 = match.team2 ?: return

        val selectedMaps = availableMaps.shuffled().take(3)
        val battle = Battle(
            team1 = team1,
            team2 = team2,
            selectedMaps = selectedMaps
        )

        _currentPlayoffMatch.value = match
        _gameState.value = _gameState.value.copy(currentBattle = battle)
        _isTournamentMatch.value = true
        _completedBattle.value = null
    }

    // === ОБНОВЛЕННАЯ ФУНКЦИЯ: Симуляция матча плей-офф с учетом силы ===
    private fun simulatePlayoffMatch(match: PlayoffMatch) {
        val team1 = match.team1 ?: return
        val team2 = match.team2 ?: return

        val maps = availableMaps.shuffled().take(3)
        val mapResults = mutableListOf<MapResult>()

        var team1Wins = 0
        var team2Wins = 0

        // Рассчитываем вероятность победы для каждой карты
        val winProbability = calculateMatchWinProbability(team1, team2)

        for ((index, map) in maps.withIndex()) {
            if (team1Wins == 2 || team2Wins == 2) break

            // Используем вероятность на основе силы для определения победителя карты
            val winner = if (Random.nextDouble() < winProbability) team1 else team2
            val winnerScore = 13
            val loserScore = Random.nextInt(0, 13)

            val team1Score = if (winner == team1) winnerScore else loserScore
            val team2Score = if (winner == team2) winnerScore else loserScore

            mapResults.add(MapResult(
                mapNumber = index + 1,
                mapName = map.getDisplayName(),
                team1Score = team1Score,
                team2Score = team2Score,
                winner = winner
            ))

            if (winner == team1) team1Wins++ else team2Wins++
        }

        match.team1Score = team1Wins
        match.team2Score = team2Wins
        match.mapResults = mapResults
        match.winner = if (team1Wins > team2Wins) team1 else team2
        match.isCompleted = true
    }

    // === ИСПРАВЛЕННАЯ ФУНКЦИЯ: Завершение матча плей-офф ===
    fun completePlayoffMatch(playerMapsWon: Int, opponentMapsWon: Int, mapResults: List<MapResult>) {
        val currentMatch = _currentPlayoffMatch.value ?: return
        val playerTeam = _gameState.value.selectedTeam ?: return
        val currentBattle = _gameState.value.currentBattle ?: return

        currentMatch.team1Score = playerMapsWon
        currentMatch.team2Score = opponentMapsWon
        currentMatch.mapResults = mapResults

        // ИСПРАВЛЕННАЯ ЛОГИКА: Правильно определяем победителя
        val isPlayerTeam1 = currentMatch.team1 == playerTeam
        currentMatch.winner = if (playerMapsWon > opponentMapsWon) {
            if (isPlayerTeam1) currentMatch.team1 else currentMatch.team2
        } else {
            if (isPlayerTeam1) currentMatch.team2 else currentMatch.team1
        }

        currentMatch.isCompleted = true

        // Сохраняем состояние перед обновлением брекета
        saveCurrentMatchState()

        // Обновляем брекет только если матч действительно завершен
        if (currentMatch.isCompleted) {
            updatePlayoffBracket()

            // Определяем, выиграл ли игрок текущий матч
            val playerWon = playerMapsWon > opponentMapsWon

            if (playerWon) {
                // Если игрок выиграл - симулируем только матчи текущего раунда без игрока
                simulateCurrentRoundMatches(playerTeam)
            } else {
                // === ОБНОВЛЕННАЯ ЛОГИКА: Если игрок проиграл - симулируем ВСЕ оставшиеся матчи турнира ===
                simulateRemainingTournamentMatches()
            }
        }

        // Сохраняем завершенную битву перед сбросом
        _completedBattle.value = currentBattle

        _currentPlayoffMatch.value = null
        _gameState.value = _gameState.value.copy(currentBattle = null)

        // Сохраняем окончательное состояние
        saveCurrentMatchState()
    }

    // === ОБНОВЛЕННАЯ ФУНКЦИЯ: Симуляция текущего раунда матчей ===
    private fun simulateCurrentRoundMatches(playerTeam: Team) {
        val bracket = _playoffBracket.value ?: return

        // Определяем текущий раунд на основе последнего завершенного матча игрока
        val currentRound = getCurrentPlayoffRound()

        when (currentRound) {
            "QF" -> {
                // Симулируем оставшиеся четвертьфиналы без игрока
                bracket.quarterFinals.forEach { match ->
                    if (match.team1 != playerTeam && match.team2 != playerTeam && !match.isCompleted) {
                        simulatePlayoffMatch(match)
                    }
                }
            }
            "SF" -> {
                // Симулируем оставшиеся полуфиналы без игрока
                bracket.semiFinals.forEach { match ->
                    if (match.team1 != playerTeam && match.team2 != playerTeam && !match.isCompleted) {
                        simulatePlayoffMatch(match)
                    }
                }
            }
            "Final" -> {
                // В финале только один матч, игрок уже сыграл
            }
        }

        updatePlayoffBracket()
    }

    // === ОБНОВЛЕННАЯ ФУНКЦИЯ: Симуляция всех оставшихся матчей турнира ===
    private fun simulateRemainingTournamentMatches() {
        val bracket = _playoffBracket.value ?: return

        // Симулируем все незавершенные матчи до определения чемпиона
        var hasUnfinishedMatches = true

        while (hasUnfinishedMatches) {
            // Симулируем четвертьфиналы
            bracket.quarterFinals.forEach { match ->
                if (!match.isCompleted) {
                    simulatePlayoffMatch(match)
                }
            }

            updatePlayoffBracket()

            // Симулируем полуфиналы
            bracket.semiFinals.forEach { match ->
                if (!match.isCompleted) {
                    simulatePlayoffMatch(match)
                }
            }

            updatePlayoffBracket()

            // Симулируем финал
            bracket.final?.let { match ->
                if (!match.isCompleted) {
                    simulatePlayoffMatch(match)
                }
            }

            updatePlayoffBracket()

            // Проверяем, остались ли незавершенные матчи
            hasUnfinishedMatches = bracket.quarterFinals.any { !it.isCompleted } ||
                    bracket.semiFinals.any { !it.isCompleted } ||
                    (bracket.final?.isCompleted == false)
        }

        // После симуляции всех матчей турнир завершен
        _selectedTournament.value = _selectedTournament.value?.copy(
            currentStage = TournamentStage.Finished,
            isCompleted = true
        )
    }

    private fun getCurrentPlayoffRound(): String {
        val bracket = _playoffBracket.value ?: return "QF"

        return when {
            bracket.quarterFinals.any { !it.isCompleted } -> "QF"
            bracket.semiFinals.any { !it.isCompleted } -> "SF"
            bracket.final?.isCompleted == false -> "Final"
            else -> "Finished"
        }
    }

    private fun updatePlayoffBracket() {
        val currentBracket = _playoffBracket.value ?: return
        val tournament = _selectedTournament.value ?: return

        val updatedBracket = when {
            // Если четвертьфиналы завершены и полуфиналы еще не созданы
            currentBracket.quarterFinals.isNotEmpty() &&
                    currentBracket.quarterFinals.all { it.isCompleted } &&
                    currentBracket.semiFinals.isEmpty() -> {

                // Проверяем, что все победители определены
                val winners = currentBracket.quarterFinals.mapNotNull { it.winner }
                if (winners.size == 4) {
                    val semiFinal1 = PlayoffMatch("SF1", winners[0], winners[1])
                    val semiFinal2 = PlayoffMatch("SF2", winners[2], winners[3])
                    currentBracket.copy(semiFinals = listOf(semiFinal1, semiFinal2))
                } else {
                    currentBracket
                }
            }

            // Если полуфиналы завершены и финал еще не создан
            currentBracket.semiFinals.isNotEmpty() &&
                    currentBracket.semiFinals.all { it.isCompleted } &&
                    currentBracket.final == null -> {

                val winners = currentBracket.semiFinals.mapNotNull { it.winner }
                if (winners.size == 2) {
                    val final = PlayoffMatch("Final", winners[0], winners[1])
                    currentBracket.copy(final = final)
                } else {
                    currentBracket
                }
            }

            // Если финал завершен, определяем чемпиона
            currentBracket.final?.isCompleted == true -> {
                currentBracket.copy(champion = currentBracket.final.winner).also {
                    _selectedTournament.value = tournament.copy(
                        currentStage = TournamentStage.Finished,
                        isCompleted = true
                    )
                }
            }
            else -> currentBracket
        }

        _playoffBracket.value = updatedBracket
        _selectedTournament.value = tournament.copy(playoffBracket = updatedBracket)
    }

    // === НОВЫЕ ФУНКЦИИ ДЛЯ ФИНАЛА ===
    fun canPlayFinal(): Boolean {
        val bracket = _playoffBracket.value ?: return false
        val playerTeam = _gameState.value.selectedTeam ?: return false

        // Проверяем, есть ли финал и участвует ли в нем игрок
        return bracket.final?.let { final ->
            !final.isCompleted && (final.team1 == playerTeam || final.team2 == playerTeam)
        } ?: false
    }

    fun startFinalMatch() {
        val bracket = _playoffBracket.value ?: return
        val final = bracket.final ?: return

        if (!final.isCompleted) {
            startPlayoffMatch(final)
        }
    }

    fun getFinalMatch(): com.example.cs2battlegame.model.PlayoffMatch? {
        return _playoffBracket.value?.final
    }

    // === НОВЫЕ ФУНКЦИИ ДЛЯ УЛУЧШЕНИЙ ===
    fun getPlayerPlayoffMatch(): PlayoffMatch? {
        val bracket = _playoffBracket.value ?: return null
        val playerTeam = _gameState.value.selectedTeam ?: return null

        return findNextPlayerPlayoffMatch(bracket, playerTeam)
    }

    fun hasPlayerQualifiedForPlayoffs(): Boolean {
        val tournament = _selectedTournament.value ?: return false
        val playerTeam = _gameState.value.selectedTeam ?: return false

        return tournament.groups.any { group ->
            group.teams.filter { it.team == playerTeam }
                .any { groupTeam -> group.teams.sortedByDescending { it.points }.indexOf(groupTeam) < 2 }
        }
    }

    enum class RoundResult {
        CONTINUE,
        PLAYER_WON_ROUND,
        OPPONENT_WON_ROUND,
        PLAYER_WON_MAP,
        OPPONENT_WON_MAP
    }

    // === ТУРНИРНЫЕ ФУНКЦИИ ===
    fun selectTournament(tournament: Tournament) {
        println("🎯 SELECT TOURNAMENT: ${tournament.name}")
        _selectedTournament.value = tournament
    }

    fun initializeTournament(tournament: Tournament, allTeams: List<Team>) {
        val top16Teams = allTeams.sortedByDescending { it.getDisplayStrength() }.take(16)
        val groups = createGroups(top16Teams)

        val updatedTournament = tournament.copy(
            groups = groups,
            currentStage = TournamentStage.GroupStage
        )

        _selectedTournament.value = updatedTournament
        println("🎯 Tournament initialized: ${tournament.name}")
    }

    private fun createGroups(teams: List<Team>): List<TournamentGroup> {
        return listOf(
            TournamentGroup("Group A", teams.slice(0..3).map { GroupTeam(it) }, generateGroupMatches(teams.slice(0..3))),
            TournamentGroup("Group B", teams.slice(4..7).map { GroupTeam(it) }, generateGroupMatches(teams.slice(4..7))),
            TournamentGroup("Group C", teams.slice(8..11).map { GroupTeam(it) }, generateGroupMatches(teams.slice(8..11))),
            TournamentGroup("Group D", teams.slice(12..15).map { GroupTeam(it) }, generateGroupMatches(teams.slice(12..15)))
        )
    }

    private fun generateGroupMatches(teams: List<Team>): List<GroupMatch> {
        val matches = mutableListOf<GroupMatch>()
        for (i in teams.indices) {
            for (j in i + 1 until teams.size) {
                matches.add(GroupMatch(teams[i], teams[j]))
            }
        }
        return matches
    }

    // === ОСНОВНЫЕ ФУНКЦИИ ИГРЫ ===
    private fun createDefaultTeams(): List<Team> {
        return listOf(
            Team("BetBoom", "Россия", 695, listOf(
                Player("Boombl4", PlayerRole.AWPER, 73),
                Player("S1ren", PlayerRole.ENTRY, 73),
                Player("d1Ledez", PlayerRole.SUPPORT, 73),
                Player("artFr0st", PlayerRole.LURKER, 73),
                Player("Magnojez", PlayerRole.IGL, 73)
            ), establishedYear = 2009, coach = "RAiLWAY"),
            Team("PARIVISION", "Россия", 710, listOf(
                Player("jame", PlayerRole.AWPER, 73),
                Player("BELCHONOKK", PlayerRole.ENTRY, 73),
                Player("AW", PlayerRole.SUPPORT, 73),
                Player("xiELO", PlayerRole.LURKER, 73),
                Player("nota", PlayerRole.IGL, 73)
            ), establishedYear = 2009, coach = "dastan"),
            Team("OG", "Международная", 720, listOf(
                Player("nicoodoz", PlayerRole.AWPER, 75),
                Player("spooke", PlayerRole.ENTRY, 75),
                Player("arrozdose", PlayerRole.SUPPORT, 75),
                Player("adamb", PlayerRole.LURKER, 75),
                Player("Chr1zN", PlayerRole.IGL, 75)
            ), establishedYear = 2009, coach = "Lambert"),
            Team("NAVI", "Украина", 795, listOf(
                Player("w0nderful", PlayerRole.AWPER, 80),
                Player("b1t", PlayerRole.ENTRY, 80),
                Player("iM", PlayerRole.SUPPORT, 80),
                Player("makazze", PlayerRole.LURKER, 80),
                Player("Aleksib", PlayerRole.IGL, 80)
            ), establishedYear = 2009, coach = "B1ad3"),
            Team("Vitality", "Франция", 860, listOf(
                Player("ZywOo", PlayerRole.AWPER, 89),
                Player("ropz", PlayerRole.ENTRY, 89),
                Player("flameZ", PlayerRole.SUPPORT, 89),
                Player("Mezii", PlayerRole.LURKER, 89),
                Player("apEX", PlayerRole.IGL, 89)
            ), establishedYear = 2013, coach = "XTQZZZ"),
            Team("Faze Clan", "Международная", 770, listOf(
                Player("broky", PlayerRole.AWPER, 84),
                Player("rain", PlayerRole.ENTRY, 84),
                Player("frozen", PlayerRole.SUPPORT, 84),
                Player("karrigan", PlayerRole.IGL, 84),
                Player("jcobbb", PlayerRole.LURKER, 84)
            ), establishedYear = 2016, coach = "NEO"),
            Team("G2 Esports", "Международная", 820, listOf(
                Player("SunPayus", PlayerRole.AWPER, 85),
                Player("malbsMd", PlayerRole.ENTRY, 85),
                Player("huNter-", PlayerRole.SUPPORT, 85),
                Player("HeavyGod", PlayerRole.IGL, 85),
                Player("MATYS", PlayerRole.LURKER, 85)
            ), establishedYear = 2017, coach = "sAw"),
            Team("Astralis", "Дания", 780, listOf(
                Player("dev1ce", PlayerRole.AWPER, 83),
                Player("Magisk", PlayerRole.ENTRY, 83),
                Player("Hooxi", PlayerRole.SUPPORT, 83),
                Player("jabbi", PlayerRole.IGL, 83),
                Player("Staehr", PlayerRole.LURKER, 83)
            ), establishedYear = 2003, coach = "ruggah"),
            Team("Virtus.pro", "Россия", 760, listOf(
                Player("ICY", PlayerRole.AWPER, 78),
                Player("FL1T", PlayerRole.ENTRY, 78),
                Player("fame", PlayerRole.SUPPORT, 78),
                Player("Perfecto", PlayerRole.IGL, 78),
                Player("tO0RO", PlayerRole.LURKER, 78)
            ), establishedYear = 2003, coach = "F_1N"),
            Team("Team Spirit", "Россия", 840, listOf(
                Player("sh1ro", PlayerRole.AWPER, 86),
                Player("donk", PlayerRole.ENTRY, 86),
                Player("tN1R", PlayerRole.SUPPORT, 86),
                Player("chopper", PlayerRole.IGL, 86),
                Player("zweix", PlayerRole.LURKER, 86)
            ), establishedYear = 2015, coach = "hally"),
            Team("Aurora", "Казахстан", 800, listOf(
                Player("MAJ3R", PlayerRole.AWPER, 83),
                Player("XANTARES", PlayerRole.ENTRY, 83),
                Player("woxic", PlayerRole.SUPPORT, 83),
                Player("Wicadia", PlayerRole.IGL, 83),
                Player("jottAAA", PlayerRole.LURKER, 83)
            ), establishedYear = 2022, coach = "Fabre"),
            Team("Falcons", "Европа", 810, listOf(
                Player("m0NESY", PlayerRole.AWPER, 85),
                Player("NiKo", PlayerRole.ENTRY, 85),
                Player("kyousuke", PlayerRole.SUPPORT, 85),
                Player("kyxsan", PlayerRole.IGL, 85),
                Player("TeSeS", PlayerRole.LURKER, 85)
            ), establishedYear = 2020, coach = "zonic"),
            Team("3DMAX", "Франция", 785, listOf(
                Player("bodyy", PlayerRole.AWPER, 80),
                Player("Lucky", PlayerRole.ENTRY, 80),
                Player("Ex3rcice", PlayerRole.SUPPORT, 80),
                Player("Maka", PlayerRole.IGL, 80),
                Player("Graviti", PlayerRole.LURKER, 80)
            ), establishedYear = 1997, coach = "YouKnow"),
            Team("The MongolZ", "Монголия", 870, listOf(
                Player("bL1tz", PlayerRole.AWPER, 87),
                Player("Techno", PlayerRole.ENTRY, 87),
                Player("Senzu", PlayerRole.SUPPORT, 87),
                Player("mzinho", PlayerRole.IGL, 87),
                Player("910", PlayerRole.LURKER, 87)
            ), establishedYear = 2007, coach = "maaRaa"),
            Team("TYLOO", "Китай", 765, listOf(
                Player("Attacker", PlayerRole.AWPER, 80),
                Player("jee", PlayerRole.ENTRY, 80),
                Player("Mercury", PlayerRole.SUPPORT, 80),
                Player("Moseyuh", PlayerRole.IGL, 80),
                Player("JamYoung", PlayerRole.LURKER, 80)
            ), establishedYear = 2007, coach = "zhokiNg"),
            Team("paIN", "Бразилия", 790, listOf(
                Player("dgt", PlayerRole.AWPER, 80),
                Player("biguzera", PlayerRole.ENTRY, 80),
                Player("dav1deuS", PlayerRole.SUPPORT, 80),
                Player("nqz", PlayerRole.IGL, 80),
                Player("snow", PlayerRole.LURKER, 80)
            ), establishedYear = 2007, coach = "rikz"),
            Team("Lynn Vision", "Китай", 740, listOf(
                Player("Westmelon", PlayerRole.AWPER, 80),
                Player("z4kr", PlayerRole.ENTRY, 80),
                Player("Starry", PlayerRole.SUPPORT, 80),
                Player("EmiliaQAQ", PlayerRole.IGL, 80),
                Player("C4LLM3SU3", PlayerRole.LURKER, 80),
            ), establishedYear = 2007, coach = "GUM"),
            Team("B8", "Украина", 735, listOf(
                Player("headtr1ck", PlayerRole.AWPER, 80),
                Player("alex666", PlayerRole.ENTRY, 80),
                Player("npl", PlayerRole.SUPPORT, 80),
                Player("kensizor", PlayerRole.IGL, 80),
                Player("esenthial", PlayerRole.LURKER, 80),
            ), establishedYear = 2007, coach = "maddened"),
            Team("Ninjas in Pyjamas", "Швеция", 725, listOf(
                Player("Snappi", PlayerRole.AWPER, 80),
                Player("sjuush", PlayerRole.ENTRY, 80),
                Player("r1nkle", PlayerRole.SUPPORT, 80),
                Player("ewjerkz", PlayerRole.IGL, 80),
                Player("xKacpersky", PlayerRole.LURKER, 80),
            ), establishedYear = 2007, coach = "Xitz"),
            Team("fnatic", "Международная", 721, listOf(
                Player("KRIMZ", PlayerRole.AWPER, 80),
                Player("blameF", PlayerRole.ENTRY, 80),
                Player("fear", PlayerRole.SUPPORT, 80),
                Player("CYPHER", PlayerRole.IGL, 80),
                Player("jambo", PlayerRole.LURKER, 80),
            ), establishedYear = 2007, coach = "Independent"),
            Team("Gentle Mates", "Испанцы", 705, listOf(
                Player("alex", PlayerRole.AWPER, 80),
                Player("mopoz", PlayerRole.ENTRY, 80),
                Player("sausol", PlayerRole.SUPPORT, 80),
                Player("dav1g", PlayerRole.IGL, 80),
                Player("MartinezSa", PlayerRole.LURKER, 80),
            ), establishedYear = 2007, coach = "deLonge"),
            Team("9INE", "Международная", 700, listOf(
                Player("raalz", PlayerRole.AWPER, 80),
                Player("faveN", PlayerRole.ENTRY, 80),
                Player("kraghen", PlayerRole.SUPPORT, 80),
                Player("cej0t", PlayerRole.IGL, 80),
                Player("Esqa", PlayerRole.LURKER, 80),
            ), establishedYear = 2007, coach = "BERRY"),
            Team("Passion UA", "Украина", 690, listOf(
                Player("JT", PlayerRole.AWPER, 80),
                Player("hallzerk", PlayerRole.ENTRY, 80),
                Player("Grim", PlayerRole.SUPPORT, 80),
                Player("Kvem", PlayerRole.IGL, 80),
                Player("nicx", PlayerRole.LURKER, 80),
            ), establishedYear = 2007, coach = "T.c"),
            Team("SAW", "Португалия", 680, listOf(
                Player("MUTiRiS", PlayerRole.AWPER, 80),
                Player("story", PlayerRole.ENTRY, 80),
                Player("Ag1l", PlayerRole.SUPPORT, 80),
                Player("aragornN", PlayerRole.IGL, 80),
                Player("krazy", PlayerRole.LURKER, 80),
            ), establishedYear = 2007, coach = "SYDOX"),
            Team("GamerLegion", "Европа", 755, listOf(
                Player("REZ", PlayerRole.AWPER, 80),
                Player("ztr", PlayerRole.ENTRY, 80),
                Player("Tauson", PlayerRole.SUPPORT, 80),
                Player("PR", PlayerRole.IGL, 80),
                Player("hypex", PlayerRole.LURKER, 80)
            ), establishedYear = 2017, coach = "ash"),
            Team("Legacy", "Бразилия", 745, listOf(
                Player("saadzin", PlayerRole.AWPER, 81),
                Player("latto", PlayerRole.ENTRY, 81),
                Player("lux", PlayerRole.SUPPORT, 81),
                Player("n1ssim", PlayerRole.IGL, 81),
                Player("dumau", PlayerRole.LURKER, 81)
            ), establishedYear = 2021, coach = "chucky"),
            Team("M80", "США", 730, listOf(
                Player("HexT", PlayerRole.AWPER, 81),
                Player("s1n", PlayerRole.ENTRY, 81),
                Player("Lake", PlayerRole.SUPPORT, 81),
                Player("Swisher", PlayerRole.IGL, 81),
                Player("slaxz-", PlayerRole.LURKER, 81)
            ), establishedYear = 2023, coach = "dephh"),
            Team("Heroic", "Дания", 750, listOf(
                Player("xfl0ud", PlayerRole.AWPER, 84),
                Player("LNZ", PlayerRole.ENTRY, 84),
                Player("nilo", PlayerRole.SUPPORT, 84),
                Player("yxngstxr", PlayerRole.IGL, 84),
                Player("Alkaren", PlayerRole.LURKER, 84)
            ), establishedYear = 2016, coach = "TOBIZ"),
            Team("Liquid", "Россия", 775, listOf(
                Player("NAF", PlayerRole.AWPER, 78),
                Player("EliGE", PlayerRole.ENTRY, 78),
                Player("nertZ", PlayerRole.IGL, 78),
                Player("siuhy", PlayerRole.SUPPORT, 78),
                Player("ultimate", PlayerRole.LURKER, 78)
            ), establishedYear = 2012, coach = "flashie"),
            Team("MOUZ", "Международная", 850, listOf(
                Player("torzsi", PlayerRole.AWPER, 86),
                Player("xertioN", PlayerRole.ENTRY, 86),
                Player("Spinx", PlayerRole.IGL, 86),
                Player("Jimpphat", PlayerRole.SUPPORT, 86),
                Player("Brollan", PlayerRole.LURKER, 86)
            ), establishedYear = 2002, coach = "sycrone"),
            Team("ENCE", "Финляндия", 715, listOf(
                Player("rigoN", PlayerRole.AWPER, 78),
                Player("sdy", PlayerRole.ENTRY, 78),
                Player("myltsi", PlayerRole.IGL, 78),
                Player("podi", PlayerRole.SUPPORT, 78),
                Player("Neityu", PlayerRole.LURKER, 78)
            ), establishedYear = 2012, coach = "enkay J"),
            Team("FURIA", "Бразилия", 830, listOf(
                Player("yuurih", PlayerRole.ENTRY, 84),
                Player("KSCERATO", PlayerRole.LURKER, 84),
                Player("FalleN", PlayerRole.IGL, 84),
                Player("YEKINDAR", PlayerRole.SUPPORT, 84),
                Player("molodoy", PlayerRole.AWPER, 84)
            ), establishedYear = 2017, coach = "sidde")
        )
    }

    private fun updateTeamsStrength() {
        val updatedTeams = _teams.value.map { team -> team.copy() }
        _teams.value = updatedTeams
    }

    fun updateSettings(newSettings: GameSettings) {
        _gameSettings.value = newSettings
        if (!newSettings.autoPlayEnabled) stopAutoPlay()
    }

    fun toggleAutoPlay() {
        val newSettings = _gameSettings.value.copy(
            autoPlayEnabled = !_gameSettings.value.autoPlayEnabled
        )
        updateSettings(newSettings)
        if (newSettings.autoPlayEnabled) startAutoPlay()
    }

    fun startAutoPlay() {
        stopAutoPlay()
        autoPlayJob = viewModelScope.launch {
            while (_gameSettings.value.autoPlayEnabled &&
                _gameState.value.currentBattle?.checkSeriesWinner() == null) {
                simulateRound()
                delay(_gameSettings.value.roundDurationMs.toLong())
            }
        }
    }

    fun stopAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = null
    }

    fun selectTeam(team: Team) {
        println("🎯 SELECT TEAM: ${team.name}")
        _gameState.value = _gameState.value.copy(selectedTeam = team)
    }

    fun showTeamStats(team: Team) {
        _selectedTeamForStats.value = team
    }

    fun hideTeamStats() {
        _selectedTeamForStats.value = null
        println("📊 STATS CLOSED")
    }

    fun startBattle(againstTeam: Team) {
        val selectedTeam = _gameState.value.selectedTeam ?: return
        val selectedMaps = availableMaps.shuffled().take(3)

        val battle = Battle(
            team1 = selectedTeam,
            team2 = againstTeam,
            selectedMaps = selectedMaps
        )

        _gameState.value = _gameState.value.copy(
            currentBattle = battle
        )

        // Устанавливаем тип матча как обычный (не турнирный)
        _isTournamentMatch.value = false

        // Обновляем статус незавершенного матча
        _hasUnfinishedGroupMatch.value = true

        // Сбрасываем завершенную битву
        _completedBattle.value = null
    }

    // ДОБАВЛЕНА ФУНКЦИЯ ДЛЯ ОДИНОЧНОГО МАТЧА
    fun startSingleMatch(againstTeam: Team) {
        startBattle(againstTeam)
    }

    fun simulateRound() {
        println("🎲 Simulating round...")
    }

    fun simulateFullMatch() {
        println("🎮 Simulating full match...")
    }

    fun resetBattle() {
        _gameState.value = _gameState.value.copy(
            currentBattle = null,
            selectedTeam = null
        )
        _hasUnfinishedGroupMatch.value = false
        _isTournamentMatch.value = false
        _completedBattle.value = null
        stopAutoPlay()
    }

    // === ТУРНИРНЫЕ ФУНКЦИИ ===
    fun simulateTournamentRound() {
        println("🎯 Simulating tournament round...")
    }

    fun simulateAllGroupMatches() {
        println("🎯 Simulating all group matches...")
    }

    fun simulateFullTournament() {
        println("🎯 Simulating full tournament...")
    }
}