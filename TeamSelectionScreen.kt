package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.utils.SafeTeamLogo
import com.example.cs2battlegame.viewmodel.GameViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TeamSelectionScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val teams = gameViewModel.teams.value.take(20)
    val selectedTeam = gameViewModel.gameState.value.selectedTeam

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Text(
            text = "🎮 ВЫБОР ВАШЕЙ КОМАНДЫ",
            color = Color(0xFF00FF88),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Text(
            text = "Выберите команду, за которую будете играть:",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        val sortedTeams = teams.sortedByDescending { it.getDisplayStrength() }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = sortedTeams,
                key = { it.name }
            ) { team ->
                SafeTeamCard(
                    team = team,
                    position = sortedTeams.indexOf(team) + 1,
                    onClick = {
                        gameViewModel.selectTeam(team)
                        navController.navigate("opponent_selection")
                    }
                )
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
        ) {
            Text("← НАЗАД К ВЫБОРУ РЕЖИМА", fontSize = 16.sp)
        }
    }
}

@Composable
fun SafeTeamCard(
    team: com.example.cs2battlegame.model.Team,
    position: Int,
    onClick: () -> Unit
) {
    TeamCard(team, position, onClick)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TeamCard(
    team: com.example.cs2battlegame.model.Team,
    position: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        backgroundColor = Color(0xFF2D2D2D),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$position",
                    color = getPositionColor(position),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(30.dp)
                )

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = team.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = team.country,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                SafeTeamLogo(teamName = team.name, modifier = Modifier.size(40.dp))

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${team.getDisplayStrength()} STR",
                        color = Color(0xFF00FF88),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${team.wins}W / ${team.losses}L",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

fun getPositionColor(position: Int): Color {
    return when (position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xFF00FF88)
    }
}