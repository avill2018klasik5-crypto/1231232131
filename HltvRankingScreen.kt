package com.example.cs2battlegame.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.cs2battlegame.utils.SafeTeamLogo
import com.example.cs2battlegame.viewmodel.GameViewModel

@Composable
fun HltvRankingScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    // === ИСПРАВЛЕНИЕ: Берем ВСЕ 32 команды ===
    val teams = gameViewModel.teams.value

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
            // Заголовок в рамке
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                backgroundColor = Color.Transparent,
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2A2A2A),
                                    Color(0xFF1E1E1E)
                                )
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "РЕЙТИНГ HLTV",
                        color = Color(0xFF00FF88),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            Text(
                text = "Топ-32 команды мира", // === ИЗМЕНЕНИЕ: Обновляем текст ===
                color = Color(0x88FFFFFF),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Список команд
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = teams.sortedByDescending { it.getDisplayStrength() },
                    key = { index, team -> team.name + index }
                ) { index, team ->
                    HltvTeamCardWithPosition(
                        team = team,
                        position = index + 1,
                        onClick = {
                            gameViewModel.showTeamStats(team)
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
fun HltvTeamCardWithPosition(
    team: com.example.cs2battlegame.model.Team,
    position: Int,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    val positionColor = when (position) {
        1 -> Color(0xFFFFD700) // Золото
        2 -> Color(0xFFC0C0C0) // Серебро
        3 -> Color(0xFFCD7F32) // Бронза
        else -> Color(0xFF00FF88) // Зеленый
    }

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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Левая часть - позиция и информация
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Позиция в рейтинге
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(positionColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$position",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Логотип
                    SafeTeamLogo(
                        teamName = team.name,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Название команды - фиксированная ширина
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = team.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = team.country,
                            color = Color(0x88FFFFFF),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                // Правая часть - статистика (фиксированная ширина)
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(80.dp)
                ) {
                    Text(
                        text = "${team.getDisplayStrength()}",
                        color = Color(0xFF00FF88),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "STR",
                        color = Color(0x66FFFFFF),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${team.wins}W • ${team.losses}L",
                        color = Color(0x66FFFFFF),
                        fontSize = 10.sp
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
                                    positionColor.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}