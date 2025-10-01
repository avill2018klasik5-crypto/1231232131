package com.example.cs2battlegame.screens

import androidx.compose.foundation.clickable

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.model.Tournaments
import com.example.cs2battlegame.viewmodel.GameViewModel

@Composable
fun TournamentSelectionScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок
            Text(
                text = "🏆 ВЫБОР ТУРНИРА",
                color = Color(0xFF00FF88),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Выберите турнир для участия",
                color = Color(0x88FFFFFF),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Список турниров
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(Tournaments.ALL_TOURNAMENTS) { tournament ->
                    ModernTournamentCard(
                        tournament = tournament,
                        onClick = {
                            gameViewModel.selectTournament(tournament)
                            navController.navigate("tournament_team_selection")
                        }
                    )
                }
            }

            // Кнопка назад
            ModernBackButton(
                onClick = { navController.popBackStack() },
                text = "НАЗАД В МЕНЮ"
            )
        }
    }
}

@Composable
fun ModernTournamentCard(
    tournament: com.example.cs2battlegame.model.Tournament,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        backgroundColor = Color.Transparent,
        elevation = if (isHovered) 12.dp else 6.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A).copy(alpha = 0.8f),
                            Color(0xFF1E1E1E).copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Заголовок и призовой фонд
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tournament.name,
                        color = Color(0xFFFFD700),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tournament.prizePool,
                        color = Color(0xFF00FF88),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Информация о турнире
                ModernTournamentInfo("🎯 Организатор", tournament.organizer)
                ModernTournamentInfo("📍 Место", tournament.location)
                ModernTournamentInfo("📅 Дата", tournament.date)
                ModernTournamentInfo("⚔️ Формат", tournament.format)
                ModernTournamentInfo("👥 Участники", "${tournament.participantCount} команд")

                Spacer(modifier = Modifier.height(12.dp))

                // Статус доступности
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00FF88))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ДОСТУПНО",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Эффект при наведении
            if (isHovered) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun ModernTournamentInfo(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color(0x88FFFFFF),
            fontSize = 14.sp,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}