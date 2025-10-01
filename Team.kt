package com.example.cs2battlegame.model

data class Team(
    val name: String,
    val country: String,
    var strength: Int,  // Сила команды от 0 до 1000
    val players: List<Player>,
    var wins: Int = 0,
    var losses: Int = 0,
    var mapsPlayed: Int = 0,
    var roundsWon: Int = 0,
    var roundsLost: Int = 0,
    val establishedYear: Int = 0,
    val coach: String = ""
) {
    fun calculateTeamStrength(): Int {
        val avgSkill = players.map { it.skill }.average().toInt()
        return (avgSkill * 10) + (wins * 5) - (losses * 2)
    }

    fun getDisplayStrength(): Int {
        return strength
    }

    fun getWinRate(): Double {
        val totalMatches = wins + losses
        return if (totalMatches > 0) (wins.toDouble() / totalMatches * 100) else 0.0
    }

    fun getRoundWinRate(): Double {
        val totalRounds = roundsWon + roundsLost
        return if (totalRounds > 0) (roundsWon.toDouble() / totalRounds * 100) else 0.0
    }

    fun getAlivePlayers(): List<Player> = players.filter { it.isAlive() }

    fun isTeamEliminated(): Boolean = getAlivePlayers().isEmpty()

    fun resetTeam() {
        players.forEach { it.resetStats() }
    }

    fun addWin() {
        wins++
    }

    fun addLoss() {
        losses++
    }

    fun addRoundsWon(count: Int) {
        roundsWon += count
    }

    fun addRoundsLost(count: Int) {
        roundsLost += count
    }

    fun addMapPlayed() {
        mapsPlayed++
    }
}