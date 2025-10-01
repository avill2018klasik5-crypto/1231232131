package com.example.cs2battlegame.model

sealed class TournamentStage {
    object NotStarted : TournamentStage()
    object GroupStage : TournamentStage()
    object Playoffs : TournamentStage()
    object Finished : TournamentStage()
}

// Добавляем класс для результатов карты
data class MapResult(
    val mapNumber: Int,
    val mapName: String,
    val team1Score: Int,
    val team2Score: Int,
    val winner: Team?
)

data class Tournament(
    val id: String,
    val name: String,
    val organizer: String,
    val location: String,
    val date: String,
    val prizePool: String,
    val format: String,
    val participantCount: Int,
    val groups: List<TournamentGroup> = emptyList(),
    val playoffBracket: PlayoffBracket? = null,
    var currentStage: TournamentStage = TournamentStage.NotStarted,
    var isCompleted: Boolean = false
)

data class TournamentGroup(
    val name: String,
    val teams: List<GroupTeam>,
    val matches: List<GroupMatch>
)

data class GroupTeam(
    val team: Team,
    var points: Int = 0,
    var wins: Int = 0,
    var losses: Int = 0,
    var roundDifference: Int = 0,
    var mapsPlayed: Int = 0
) {
    fun addWin(roundDiff: Int) {
        wins++
        points += 3
        roundDifference += roundDiff
        mapsPlayed++
    }

    fun addLoss(roundDiff: Int) {
        losses++
        roundDifference -= roundDiff
        mapsPlayed++
    }
}

data class GroupMatch(
    val team1: Team,
    val team2: Team,
    var team1Score: Int = 0, // Счет по картам (2:0, 2:1 и т.д.)
    var team2Score: Int = 0,
    var mapResults: List<MapResult> = emptyList(), // Результаты отдельных карт
    var isPlayed: Boolean = false,
    var winner: Team? = null
)

data class PlayoffBracket(
    val quarterFinals: List<PlayoffMatch> = emptyList(),
    val semiFinals: List<PlayoffMatch> = emptyList(),
    val final: PlayoffMatch? = null,
    val champion: Team? = null
)

data class PlayoffMatch(
    val matchId: String,
    val team1: Team?,
    val team2: Team?,
    var team1Score: Int = 0, // Счет по картам BO3
    var team2Score: Int = 0,
    var mapResults: List<MapResult> = emptyList(),
    var winner: Team? = null,
    var isCompleted: Boolean = false
)

data class PlayerMatch(
    val opponent: Team,
    var isPlayed: Boolean = false,
    var playerScore: Int = 0, // Счет по картам BO3 (2 или 1)
    var opponentScore: Int = 0, // Счет по картам BO3 (2 или 1)
    var mapResults: List<MapResult> = emptyList(), // Результаты отдельных карт
    var winner: Team? = null,
    val maps: List<MapPool> = emptyList()
)

// Обновленный объект с турнирами
object Tournaments {
    private val top16Teams = listOf(
        "Vitality", "Team Spirit", "MOUZ", "G2 Esports", "Faze Clan",
        "NAVI", "Falcons", "Astralis", "Virtus.pro", "Heroic",
        "Cloud9", "ENCE", "FURIA", "Aurora", "paiN Gaming", "The MongolZ"
    )

    val BLAST_AUSTIN_2025 = Tournament(
        id = "blast_austin_2025",
        name = "BLAST Austin Major 2025",
        organizer = "BLAST Premier",
        location = "Austin, Texas",
        date = "May 15-25, 2025",
        prizePool = "$500,000",
        format = "Group Stage + Playoffs",
        participantCount = 16
    )

    val ALL_TOURNAMENTS = listOf(BLAST_AUSTIN_2025)
}