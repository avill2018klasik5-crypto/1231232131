package com.example.cs2battlegame.model

import kotlin.random.Random

// Типы победы в раунде
enum class RoundWinType {
    BOMB_EXPLOSION,      // Взрыв бомбы (Т сторона)
    ELIMINATION,         // Уничтожение всех противников
    BOMB_DEFUSE,         // Обезвреживание бомбы (КТ сторона)
    TIME_EXPIRED         // Время вышло (КТ сторона)
}

// Событие убийства / действия в раунде для PlayByPlay
data class KillEvent(
    val killer: String,
    val killerTeam: String,
    val victim: String,
    val victimTeam: String,
    val weapon: String,      // например "ak47", "awp", "knife"
    val headshot: Boolean = false
)

data class Battle(
    val team1: Team,
    val team2: Team,
    var currentMap: Int = 1,
    var selectedMaps: List<MapPool> = emptyList(),
    var team1Maps: Int = 0,
    var team2Maps: Int = 0,
    var round: Int = 1,
    var team1Score: Int = 0,
    var team2Score: Int = 0,
    var isTeam1Attacking: Boolean = true,
    var isFirstHalf: Boolean = true,
    var lastRoundWinner: Team? = null,
    var isSeriesFinished: Boolean = false,
    var isOvertime: Boolean = false,           // Флаг овертайма
    var isSuperOvertime: Boolean = false       // Флаг супер овертайма
) {
    // Старый лог (строки) оставляем для совместимости
    private val _battleLog: MutableList<String> = mutableListOf()
    val battleLog: List<String> get() = _battleLog.toList()

    // Новый список структурированных событий для PlayByPlay
    private val _battleEvents: MutableList<KillEvent> = mutableListOf()
    val battleEvents: List<KillEvent> get() = _battleEvents.toList()

    // Статистика игроков для текущей карты
    private val currentMapStats = mutableMapOf<String, PlayerMapStats>()

    // Статистика за всю серию
    val seriesStats = mutableMapOf<String, PlayerSeriesStats>()

    data class PlayerMapStats(
        var kills: Int = 0,
        var deaths: Int = 0,
        var assists: Int = 0
    )

    data class PlayerSeriesStats(
        var totalKills: Int = 0,
        var totalDeaths: Int = 0,
        var totalAssists: Int = 0,
        var mapsPlayed: Int = 0
    )

    // Статистика игроков для отображения (текущая карта)
    val team1Players: List<Player> get() = team1.players.map { player ->
        val stats = currentMapStats[getPlayerKey(team1.name, player.name)] ?: PlayerMapStats()
        player.copy(
            kills = stats.kills,
            deaths = stats.deaths,
            assists = stats.assists
        )
    }

    val team2Players: List<Player> get() = team2.players.map { player ->
        val stats = currentMapStats[getPlayerKey(team2.name, player.name)] ?: PlayerMapStats()
        player.copy(
            kills = stats.kills,
            deaths = stats.deaths,
            assists = stats.assists
        )
    }

    // === ИСПРАВЛЕНИЕ: Добавляем расчет вероятности победы на основе силы ===
    private fun calculateWinProbability(attackingTeam: Team, defendingTeam: Team): Double {
        val attackingStrength = attackingTeam.getDisplayStrength().toDouble()
        val defendingStrength = defendingTeam.getDisplayStrength().toDouble()
        val totalStrength = attackingStrength + defendingStrength
        val baseProbability = 0.5
        val strengthDifference = (attackingStrength - defendingStrength) / (if (totalStrength == 0.0) 1.0 else totalStrength)
        val adjustedProbability = baseProbability + (strengthDifference * 0.3)
        return adjustedProbability.coerceIn(0.2, 0.8)
    }

    fun addToLog(message: String) {
        _battleLog.add(message)
    }

    // Добавить структурированное событие
    private fun addKillEvent(event: KillEvent) {
        _battleEvents.add(event)
        // также добавим в старый лог для совместимости
        addToLog("${event.killer} (${event.killerTeam}) -> ${event.victim} (${event.victimTeam}) with ${event.weapon}${if (event.headshot) " [HS]" else ""}")
    }

    fun getRoundInfo(): String {
        val attackingTeam = if (isTeam1Attacking) team1.name else team2.name
        val defendingTeam = if (isTeam1Attacking) team2.name else team1.name
        val side = if (isTeam1Attacking) "T" else "CT"
        val currentMapName = if (selectedMaps.isNotEmpty() && currentMap <= selectedMaps.size) {
            selectedMaps[currentMap - 1].getDisplayName()
        } else {
            "Карта $currentMap"
        }

        val overtimeInfo = when {
            isSuperOvertime -> " | СУПЕР ОВЕРТАЙМ (до 21)"
            isOvertime -> " | ОВЕРТАЙМ (до 16)"
            else -> ""
        }

        return "$currentMapName | Раунд $round: $attackingTeam ($side) vs $defendingTeam (${if (side == "T") "CT" else "T"})$overtimeInfo"
    }

    fun getCurrentMapName(): String {
        return if (selectedMaps.isNotEmpty() && currentMap <= selectedMaps.size) {
            selectedMaps[currentMap - 1].getDisplayName()
        } else {
            "Карта $currentMap"
        }
    }

    // === НОВАЯ ФУНКЦИЯ: Проверка начала овертайма ===
    private fun checkOvertimeStart() {
        if (!isOvertime && team1Score == 12 && team2Score == 12) {
            isOvertime = true
            addToLog("=== НАЧАЛСЯ ОВЕРТАЙМ! ИГРА ДО 16 РАУНДОВ ===")
        }

        if (isOvertime && !isSuperOvertime && team1Score == 15 && team2Score == 15) {
            isSuperOvertime = true
            addToLog("=== НАЧАЛСЯ СУПЕР ОВЕРТАЙМ! ИГРА ДО 21 РАУНДА ===")
        }
    }

    // === ИСПРАВЛЕННАЯ ФУНКЦИЯ: Симуляция раунда С УЧЕТОМ СИЛЫ ===
    fun simulateRound(): RoundResult {
        // Сбрасываем здоровье всех игроков для нового раунда
        team1.players.forEach { it.health = 100 }
        team2.players.forEach { it.health = 100 }

        val attackingTeam = if (isTeam1Attacking) team1 else team2
        val defendingTeam = if (isTeam1Attacking) team2 else team1

        // === ИСПРАВЛЕНИЕ: Используем силу команд для расчета вероятности ===
        val winProbability = calculateWinProbability(attackingTeam, defendingTeam)

        // Определяем победителя раунда на основе силы
        val randomValue = Random.nextDouble()
        val winningTeam = if (randomValue < winProbability) attackingTeam else defendingTeam

        // Определяем тип победы
        val winType = determineRoundWinType(winningTeam, attackingTeam, defendingTeam, Random)

        // Сохраняем победителя
        lastRoundWinner = winningTeam

        // Симулируем бой и подсчитываем статистику (и добавляем события)
        simulatePlayerCombatWithStats(attackingTeam, defendingTeam, winType, winningTeam, Random)

        // Обновляем счет карты
        if (winningTeam == team1) {
            team1Score++
        } else {
            team2Score++
        }

        round++

        // Проверяем начало овертайма
        checkOvertimeStart()

        // Проверяем победу на карте (с учетом овертаймов)
        val mapWinner = checkMapWinner()
        if (mapWinner != null) {
            // Сохраняем статистику карты в общую статистику серии
            saveMapStatsToSeries()

            if (mapWinner == team1) {
                team1Maps++
            } else {
                team2Maps++
            }

            // Проверяем победу в серии
            val seriesWinner = checkSeriesWinner()
            if (seriesWinner != null) {
                isSeriesFinished = true
                addToLog("=== СЕРИЯ ЗАВЕРШЕНА: $seriesWinner ПОБЕДИЛ СО СЧЕТОМ $team1Maps:$team2Maps ===")
                return RoundResult.SERIES_FINISHED
            } else {
                // Переходим на следующую карту
                val hasNextMap = startNextMap()
                if (hasNextMap) {
                    addToLog("=== ПЕРЕХОД НА СЛЕДУЮЩУЮ КАРТУ ===")
                    return RoundResult.NEXT_MAP
                }
            }
        }

        // Меняем стороны после 12 раундов (только если не в овертайме)
        if (round > 12 && isFirstHalf && !isOvertime) {
            switchSides()
            return RoundResult.SIDE_SWITCH
        }

        // В овертайме меняем стороны каждые 3 раунда
        if (isOvertime && ((round - 24) % 6 == 0)) {
            switchSides()
            return RoundResult.SIDE_SWITCH
        }

        return RoundResult.CONTINUE
    }

    private fun saveMapStatsToSeries() {
        // Сохраняем статистику текущей карты в общую статистику серии
        currentMapStats.forEach { (playerKey, mapStats) ->
            val seriesStat = seriesStats.getOrPut(playerKey) { PlayerSeriesStats() }
            seriesStat.totalKills += mapStats.kills
            seriesStat.totalDeaths += mapStats.deaths
            seriesStat.totalAssists += mapStats.assists
            seriesStat.mapsPlayed++
        }

        // Сбрасываем статистику для следующей карты
        currentMapStats.clear()
        // Сбрасываем события (или можно оставить, если хочешь историю всей серии)
        _battleEvents.clear()
    }

    private fun determineRoundWinType(winningTeam: Team, attackingTeam: Team, defendingTeam: Team, random: Random): RoundWinType {
        val isAttackerWon = winningTeam == attackingTeam

        return if (isAttackerWon) {
            when {
                random.nextDouble() < 0.6 -> RoundWinType.BOMB_EXPLOSION
                else -> RoundWinType.ELIMINATION
            }
        } else {
            when {
                random.nextDouble() < 0.4 -> RoundWinType.BOMB_DEFUSE
                random.nextDouble() < 0.7 -> RoundWinType.TIME_EXPIRED
                else -> RoundWinType.ELIMINATION
            }
        }
    }

    private fun simulatePlayerCombatWithStats(attackingTeam: Team, defendingTeam: Team, winType: RoundWinType, winningTeam: Team, random: Random) {
        when (winType) {
            RoundWinType.BOMB_EXPLOSION -> {
                simulateBombExplosion(attackingTeam, defendingTeam, winningTeam, random)
            }
            RoundWinType.BOMB_DEFUSE -> {
                simulateBombDefuse(attackingTeam, defendingTeam, winningTeam, random)
            }
            RoundWinType.ELIMINATION -> {
                simulateElimination(attackingTeam, defendingTeam, winningTeam, random)
            }
            RoundWinType.TIME_EXPIRED -> {
                simulateTimeExpired(attackingTeam, defendingTeam, winningTeam, random)
            }
        }
    }

    private fun simulateBombExplosion(attackingTeam: Team, defendingTeam: Team, winningTeam: Team, random: Random) {
        val strengthRatio = calculateStrengthRatio(attackingTeam, defendingTeam)
        val attackerLosses = (1 + (strengthRatio * 2)).toInt().coerceIn(1, 3)
        val defenderLosses = (3 + ((1 - strengthRatio) * 3)).toInt().coerceIn(3, 5)

        eliminatePlayersWithStats(attackingTeam, attackerLosses, defendingTeam, random)
        eliminatePlayersWithStats(defendingTeam, defenderLosses, attackingTeam, random)
    }

    private fun simulateBombDefuse(attackingTeam: Team, defendingTeam: Team, winningTeam: Team, random: Random) {
        val strengthRatio = calculateStrengthRatio(defendingTeam, attackingTeam)
        val defenderLosses = (1 + (strengthRatio * 2)).toInt().coerceIn(1, 3)
        val attackerLosses = (3 + ((1 - strengthRatio) * 3)).toInt().coerceIn(3, 5)

        eliminatePlayersWithStats(attackingTeam, attackerLosses, defendingTeam, random)
        eliminatePlayersWithStats(defendingTeam, defenderLosses, attackingTeam, random)
    }

    private fun simulateElimination(attackingTeam: Team, defendingTeam: Team, winningTeam: Team, random: Random) {
        val strengthRatio = calculateStrengthRatio(winningTeam, if (winningTeam == attackingTeam) defendingTeam else attackingTeam)
        val losingTeam = if (winningTeam == attackingTeam) defendingTeam else attackingTeam

        eliminatePlayersWithStats(losingTeam, 5, winningTeam, random)
        val survivors = (2 + (strengthRatio * 3)).toInt().coerceIn(1, 4)
        eliminatePlayersWithStats(winningTeam, 5 - survivors, losingTeam, random)
    }

    private fun simulateTimeExpired(attackingTeam: Team, defendingTeam: Team, winningTeam: Team, random: Random) {
        val strengthRatio = calculateStrengthRatio(defendingTeam, attackingTeam)
        val attackerLosses = (3 + ((1 - strengthRatio) * 3)).toInt().coerceIn(3, 5)
        val defenderLosses = (1 + (strengthRatio * 2)).toInt().coerceIn(1, 3)

        eliminatePlayersWithStats(attackingTeam, attackerLosses, defendingTeam, random)
        eliminatePlayersWithStats(defendingTeam, defenderLosses, attackingTeam, random)
    }

    private fun calculateStrengthRatio(strongerTeam: Team, weakerTeam: Team): Double {
        val strongerStrength = strongerTeam.getDisplayStrength().toDouble()
        val weakerStrength = weakerTeam.getDisplayStrength().toDouble()
        val totalStrength = strongerStrength + weakerStrength
        return if (totalStrength == 0.0) 0.0 else (strongerStrength - weakerStrength) / totalStrength
    }

    private fun eliminatePlayersWithStats(team: Team, count: Int, opposingTeam: Team, random: Random) {
        val alivePlayers = team.players.filter { it.isAlive() }
        val playersToEliminate = alivePlayers.shuffled().take(minOf(count, alivePlayers.size))

        playersToEliminate.forEach { player ->
            // наносим "смерть"
            player.takeDamage(100)

            // выбираем убийцу — случайный живой игрок противоположной команды
            val potentialKillers = opposingTeam.players.filter { it.isAlive() }
            if (potentialKillers.isNotEmpty()) {
                val killer = potentialKillers.random()
                addKillToStats(opposingTeam.name, killer.name)

                // определяем оружие рандомно (можешь заменить логику)
                val weapon = chooseRandomWeapon(random)

                // headshot с небольшой вероятностью
                val hs = random.nextDouble() < 0.18

                // добавляем событие в battleEvents
                addKillEvent(
                    KillEvent(
                        killer = killer.name,
                        killerTeam = opposingTeam.name,
                        victim = player.name,
                        victimTeam = team.name,
                        weapon = weapon,
                        headshot = hs
                    )
                )

                // Начисляем помощи другим игрокам
                if (random.nextDouble() < 0.3) {
                    opposingTeam.players.filter { it.isAlive() && it != killer }.shuffled().take(1).forEach {
                        addAssistToStats(opposingTeam.name, it.name)
                    }
                }
            }

            // Добавляем смерть игроку
            addDeathToStats(team.name, player.name)
        }
    }

    private fun chooseRandomWeapon(random: Random): String {
        return when (random.nextInt(0, 6)) {
            0 -> "ak47"
            1 -> "awp"
            2 -> "m4"
            3 -> "usp"
            4 -> "knife"
            else -> "default"
        }
    }

    private fun addKillToStats(teamName: String, playerName: String) {
        val key = getPlayerKey(teamName, playerName)
        val stats = currentMapStats.getOrPut(key) { PlayerMapStats() }
        stats.kills++
    }

    private fun addDeathToStats(teamName: String, playerName: String) {
        val key = getPlayerKey(teamName, playerName)
        val stats = currentMapStats.getOrPut(key) { PlayerMapStats() }
        stats.deaths++
    }

    private fun addAssistToStats(teamName: String, playerName: String) {
        val key = getPlayerKey(teamName, playerName)
        val stats = currentMapStats.getOrPut(key) { PlayerMapStats() }
        stats.assists++
    }

    private fun getPlayerKey(teamName: String, playerName: String): String {
        return "${teamName}_${playerName}"
    }

    // === ПОЛНОСТЬЮ ПЕРЕРАБОТАННАЯ ФУНКЦИЯ: Проверка победителя на карте с учетом овертаймов ===
    fun checkMapWinner(): Team? {
        return when {
            // Обычная игра: победа при 13 раундах
            !isOvertime && team1Score >= 13 -> team1
            !isOvertime && team2Score >= 13 -> team2

            // Овертайм: победа при 16 раундах
            isOvertime && !isSuperOvertime && team1Score >= 16 -> team1
            isOvertime && !isSuperOvertime && team2Score >= 16 -> team2

            // Супер овертайм: победа при 21 раунде
            isSuperOvertime && team1Score >= 21 -> team1
            isSuperOvertime && team2Score >= 21 -> team2

            else -> null
        }
    }

    fun checkSeriesWinner(): Team? {
        return when {
            team1Maps >= 2 -> team1
            team2Maps >= 2 -> team2
            else -> null
        }
    }

    fun switchSides() {
        isTeam1Attacking = !isTeam1Attacking
        isFirstHalf = false
        addToLog("=== СМЕНА СТОРОН ===")
    }

    fun startNextMap(): Boolean {
        if (currentMap < 3 && currentMap < selectedMaps.size) {
            currentMap++
            round = 1
            team1Score = 0
            team2Score = 0
            isTeam1Attacking = currentMap % 2 == 1
            isFirstHalf = true
            lastRoundWinner = null
            isOvertime = false
            isSuperOvertime = false

            // Восстанавливаем здоровье игроков
            team1.players.forEach { it.health = 100 }
            team2.players.forEach { it.health = 100 }

            val mapName = selectedMaps[currentMap - 1].getDisplayName()
            addToLog("=== НАЧАЛО КАРТЫ $currentMap: $mapName ===")
            return true
        }
        return false
    }

    // === ПОЛНОСТЬЮ ПЕРЕРАБОТАННАЯ ФУНКЦИЯ: Симуляция всей серии до конца ===
    fun simulateFullSeries() {
        addToLog("=== НАЧАЛО ПОЛНОЙ СИМУЛЯЦИИ МАТЧА ===")

        // Симулируем пока серия не завершится
        while (!isSeriesFinished) {
            val result = simulateRound()

            if (result == RoundResult.SERIES_FINISHED) {
                break
            }
        }

        addToLog("=== ПОЛНАЯ СИМУЛЯЦИЯ МАТЧА ЗАВЕРШЕНА ===")
    }

    // Получение суммарной статистики игрока за серию
    fun getSeriesStatsForPlayer(teamName: String, playerName: String): PlayerSeriesStats? {
        return seriesStats[getPlayerKey(teamName, playerName)]
    }

    // Восстановление здоровья игроков при создании битвы
    init {
        team1.players.forEach { it.health = 100 }
        team2.players.forEach { it.health = 100 }
    }
}

// Результаты раунда
enum class RoundResult {
    CONTINUE,
    SIDE_SWITCH,
    NEXT_MAP,
    SERIES_FINISHED
}