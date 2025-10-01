package com.example.cs2battlegame.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cs2battlegame.screens.*

@Composable
fun AppNavigation(gameViewModel: com.example.cs2battlegame.viewmodel.GameViewModel) {
    val navController = rememberNavController()

    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = "main_menu"
    ) {
        composable("main_menu") {
            MainMenuScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("game_mode_selection") {
            GameModeSelectionScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("hltv_ranking") {
            HltvRankingScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("single_match_team_selection") {
            SingleMatchTeamSelectionScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("single_match_opponent_selection") {
            SingleMatchOpponentSelectionScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("player_match") {
            PlayerMatchScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("tournament_team_selection") {
            TournamentTeamSelectionScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("tournament_selection") {
            TournamentSelectionScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("tournament_overview") {
            TournamentOverviewScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("group_stage") {
            GroupStageScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("playoff_bracket") {
            PlayoffBracketScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("tournament_standings") {
            TournamentStandingsScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("tournament_results") {
            TournamentResultsScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("battle") {
            NewBattleScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("playoff_battle") {
            PlayoffBattleScreen(navController = navController, gameViewModel = gameViewModel)
        }
        composable("settings") {
            SettingsScreen(navController = navController, gameViewModel = gameViewModel)
        }
    }
}