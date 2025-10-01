package com.example.cs2battlegame.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cs2battlegame.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun MainMenuScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),  // Более серый фон
                        Color(0xFF2D2D2D),
                        Color(0xFF3C3C3C)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Заголовок
            HeaderSection()

            // Основные опции меню - теперь занимает все доступное пространство
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                MenuOptionsSection(navController, gameViewModel)
            }

            // Футер с информацией
            FooterSection()
        }
    }
}

@Composable
fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Логотип игры
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00FF88),
                            Color(0xFF00CC66)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CS2",
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Основной заголовок
        Text(
            text = "BATTLE ARENA",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Подзаголовок
        Text(
            text = "КИБЕРСПОРТИВНЫЙ СИМУЛЯТОР",
            color = Color(0xFF00FF88),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )

        // Статистика игрока
        PlayerStatsBadge()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerStatsBadge() {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF333333),
                                Color(0xFF1A1A1A),
                                Color(0xFF0A0A0A)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Статистика 1 - КОМАНДЫ
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "32",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "КОМАНДЫ",
                            color = Color(0xFF00FF88),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }

                    // Разделитель
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color(0x44FFFFFF))
                    )

                    // Статистика 2 - РЕЙТИНГ
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ТОП-32",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "РЕЙТИНГ\nHLTV",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }

                    // Разделитель
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color(0x44FFFFFF))
                    )

                    // Статистика 3 - ФОРМАТ
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "BO3 | BO5",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ФОРМАТ",
                            color = Color(0xFF6200EE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuOptionsSection(navController: NavHostController, gameViewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Сетка карточек меню
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Левая колонка
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuCard(
                        title = "ТУРНИР",
                        subtitle = "Реальные турниры",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFF00FF88),
                        onClick = { navController.navigate("tournament_selection") },
                        modifier = Modifier.height(140.dp)
                    )

                    MenuCard(
                        title = "TEAM VS TEAM",  // Измененная надпись
                        subtitle = "Быстрая игра",
                        icon = Icons.Default.PlayArrow,
                        color = Color(0xFFFF9800),
                        onClick = { navController.navigate("single_match_team_selection") },
                        modifier = Modifier.height(120.dp)
                    )
                }

                // Правая колонка
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuCard(
                        title = "РЕЙТИНГ",
                        subtitle = "ТОП-32 команд",
                        icon = Icons.Default.Star,
                        color = Color(0xFFFFD700),
                        onClick = { navController.navigate("hltv_ranking") },
                        modifier = Modifier.height(120.dp)
                    )

                    MenuCard(
                        title = "НАСТРОЙКИ",
                        subtitle = "Персонализация",
                        icon = Icons.Default.Settings,
                        color = Color(0xFF6200EE),
                        onClick = { navController.navigate("settings") },
                        modifier = Modifier.height(140.dp)
                    )
                }
            }
        }

        // Spacer для опускания кнопки выхода
        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка выхода - теперь опущена ниже
        ExitButton(
            onClick = {
                android.os.Process.killProcess(android.os.Process.myPid())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    Card(
        modifier = modifier
            .scale(scale),
        backgroundColor = Color.Transparent,
        elevation = if (isHovered) 16.dp else 8.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = onClick
                )
                .background(
                    // Серый градиент с неоновым эффектом
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF404040).copy(alpha = 0.8f),
                            Color(0xFF2A2A2A).copy(alpha = 0.6f),
                            Color(0xFF1E1E1E).copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(14.dp), // Еще немного уменьшили отступы
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Иконка
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Заголовок - уменьшен для TEAM VS TEAM
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = if (title == "TEAM VS TEAM") 14.sp else 15.sp, // Меньший размер для TEAM VS TEAM
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp),
                    lineHeight = if (title == "TEAM VS TEAM") 16.sp else 17.sp
                )

                // Подзаголовок
                Text(
                    text = subtitle,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 13.sp
                )

                // Индикатор при наведении
                if (isHovered) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth()
                            .background(color)
                    )
                }
            }

            // Угловой акцент - серый неоновый эффект
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(55.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.4f),
                                color.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 40.dp, topEnd = 20.dp)
                    )
            )
        }
    }
}

@Composable
fun ExitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    Card(
        modifier = modifier
            .scale(scale),
        backgroundColor = Color.Transparent,
        elevation = if (isHovered) 12.dp else 6.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = onClick
                )
                .background(
                    // Серый градиент для кнопки выхода
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF404040).copy(alpha = 0.7f),
                            Color(0xFF2A2A2A).copy(alpha = 0.5f),
                            Color(0xFF1E1E1E).copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Выход",
                    tint = Color(0xFFFF4444),
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "ВЫХОД ИЗ ИГРЫ",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Анимация при наведении - серый неоновый эффект
            if (isHovered) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF4444).copy(alpha = 0.1f),
                                    Color(0xAAFF4444).copy(alpha = 0.05f),
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
fun FooterSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Версия игры
        Text(
            text = "CS2 BATTLE ARENA v1.0",
            color = Color(0x88FFFFFF),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Copyright
        Text(
            text = "© 2024 Кибеспортивный симулятор. Все права защищены.",
            color = Color(0x66FFFFFF),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}