package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cs2battlegame.utils.SafeTeamLogo
import com.example.cs2battlegame.viewmodel.GameViewModel

@Composable
fun TeamStatsScreen(
    gameViewModel: GameViewModel
) {
    val selectedTeam = gameViewModel.selectedTeamForStats.value

    if (selectedTeam != null) {
        Dialog(
            onDismissRequest = {
                gameViewModel.hideTeamStats()
            }
        ) {
            TeamStatsDialog(
                team = selectedTeam,
                onClose = {
                    gameViewModel.hideTeamStats()
                }
            )
        }
    }
}

@Composable
fun TeamStatsDialog(team: com.example.cs2battlegame.model.Team, onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFF2D2D2D),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с кнопкой закрытия - ИСПРАВЛЕНО
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 СТАТИСТИКА КОМАНДЫ",
                    color = Color(0xFF00FF88),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onClose
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Информация о команде
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = team.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${team.country} • Основана в ${team.establishedYear}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Тренер: ${team.coach}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                SafeTeamLogo(teamName = team.name, modifier = Modifier.size(60.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Статистика команды
            Text(
                text = "Общая статистика:",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Карточки статистики
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Победы",
                    value = team.wins.toString(),
                    color = Color(0xFF00FF88),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatCard(
                    title = "Поражения",
                    value = team.losses.toString(),
                    color = Color(0xFFFF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Винрейт",
                    value = "${String.format("%.1f", team.getWinRate())}%",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatCard(
                    title = "Карт сыграно",
                    value = team.mapsPlayed.toString(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Состав команды
            Text(
                text = "Состав команды:",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(team.players) { player ->
                    PlayerCard(player = player)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Сила команды
            Text(
                text = "Общая сила команды: ${team.getDisplayStrength()}",
                color = Color(0xFF00FF88),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Кнопка закрытия внизу - ИСПРАВЛЕНА
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
            ) {
                Text("ЗАКРЫТЬ СТАТИСТИКУ", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        backgroundColor = Color(0xFF3D3D3D),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PlayerCard(player: com.example.cs2battlegame.model.Player) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        backgroundColor = Color(0xFF3D3D3D),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = getRoleDisplayName(player.role),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "${player.skill} SKL",
                color = Color(0xFF00FF88),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getRoleDisplayName(role: com.example.cs2battlegame.model.PlayerRole): String {
    return when (role) {
        com.example.cs2battlegame.model.PlayerRole.AWPER -> "Снайпер"
        com.example.cs2battlegame.model.PlayerRole.ENTRY -> "Энтри фрагер"
        com.example.cs2battlegame.model.PlayerRole.SUPPORT -> "Саппорт"
        com.example.cs2battlegame.model.PlayerRole.LURKER -> "Леркер"
        com.example.cs2battlegame.model.PlayerRole.IGL -> "Капитан"
    }
}