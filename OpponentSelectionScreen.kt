package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.utils.TeamLogo
import com.example.cs2battlegame.viewmodel.GameViewModel

@Composable
fun OpponentSelectionScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val teams = gameViewModel.teams.value
    val selectedTeam = gameViewModel.gameState.value.selectedTeam

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "⚔️ ВЫБОР ПРОТИВНИКА",
            color = Color(0xFFFF4444),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Text(
            text = "Ваша команда: ${selectedTeam?.name ?: "Не выбрана"}",
            color = Color(0xFF00FF88),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text(
            text = "Выберите команду противника:",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Список команд противника
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(teams.filter { it != selectedTeam }.sortedByDescending { it.getDisplayStrength() }) { index, team ->
                OpponentTeamCard(
                    team = team,
                    onClick = {
                        selectedTeam?.let { myTeam ->
                            gameViewModel.startBattle(team)
                            navController.navigate("battle")
                        }
                    }
                )
            }
        }

        // Кнопка назад
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
        ) {
            Text("← НАЗАД К ВЫБОРУ КОМАНДЫ", fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OpponentTeamCard(team: com.example.cs2battlegame.model.Team, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF2D2D2D),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Название команды
            Column {
                Text(
                    text = team.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = team.country,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Логотип и статистика
            Row(verticalAlignment = Alignment.CenterVertically) {
                TeamLogo(teamName = team.name, modifier = Modifier.size(40.dp))

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