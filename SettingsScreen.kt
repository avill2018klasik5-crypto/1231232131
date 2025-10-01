package com.example.cs2battlegame.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.cs2battlegame.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel
) {
    val gameSettings by gameViewModel.gameSettings.collectAsState()

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
                        text = "НАСТРОЙКИ",
                        color = Color(0xFF00FF88),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Основной контент настроек
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Настройки автосимуляции
                item {
                    ModernSettingsCard(
                        title = "АВТОСИМУЛЯЦИЯ",
                        icon = "⚡",
                        color = Color(0xFFFF9800)
                    ) {
                        Column {
                            // Переключатель автосимуляции
                            ModernSwitchSetting(
                                label = "Автоматическая симуляция раундов",
                                isChecked = gameSettings.autoPlayEnabled,
                                onCheckedChange = { gameViewModel.toggleAutoPlay() },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Слайдер скорости симуляции
                            if (gameSettings.autoPlayEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                ModernSliderSetting(
                                    label = "Скорость симуляции",
                                    value = gameSettings.roundDurationMs.toFloat(),
                                    onValueChange = { newValue ->
                                        val newSettings = gameSettings.copy(roundDurationMs = newValue.toInt())
                                        gameViewModel.updateSettings(newSettings)
                                    },
                                    valueRange = 500f..3000f,
                                    valueDisplay = "${gameSettings.roundDurationMs}мс",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Информация о автосимуляции
                                Text(
                                    text = "• Автосимуляция работает в режиме битвы\n• Раунды симулируются автоматически\n• Можно остановить в любой момент",
                                    color = Color(0x88FFFFFF),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }

                // Настройки звука
                item {
                    ModernSettingsCard(
                        title = "ЗВУК И МУЗЫКА",
                        icon = "🎵",
                        color = Color(0xFF2196F3)
                    ) {
                        Column {
                            ModernSwitchSetting(
                                label = "Звуковые эффекты",
                                isChecked = gameSettings.soundEnabled,
                                onCheckedChange = {
                                    val newSettings = gameSettings.copy(soundEnabled = it)
                                    gameViewModel.updateSettings(newSettings)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ModernSwitchSetting(
                                label = "Фоновая музыка",
                                isChecked = gameSettings.musicEnabled,
                                onCheckedChange = {
                                    val newSettings = gameSettings.copy(musicEnabled = it)
                                    gameViewModel.updateSettings(newSettings)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Настройки уведомлений
                item {
                    ModernSettingsCard(
                        title = "УВЕДОМЛЕНИЯ",
                        icon = "🔔",
                        color = Color(0xFFFFD700)
                    ) {
                        ModernSwitchSetting(
                            label = "Уведомления о матчах",
                            isChecked = gameSettings.notificationsEnabled,
                            onCheckedChange = {
                                val newSettings = gameSettings.copy(notificationsEnabled = it)
                                gameViewModel.updateSettings(newSettings)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка назад
            ModernBackButton(
                onClick = {
                    gameViewModel.restoreMatchState()
                    navController.popBackStack()
                },
                text = "НАЗАД В МЕНЮ"
            )
        }
    }
}

@Composable
fun ModernSettingsCard(
    title: String,
    icon: String,
    color: Color,
    content: @Composable () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.01f else 1f,
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
                // Заголовок карточки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = icon,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = title,
                            color = color,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Содержимое карточки
                content()
            }

            // Эффект при наведении (ИСПРАВЛЕНО: убрана ошибка matchParentSize)
            if (isHovered) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.1f),
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
fun ModernSwitchSetting(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    Row(
        modifier = modifier
            .scale(scale)
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00FF88),
                checkedTrackColor = Color(0xFF00FF88).copy(alpha = 0.5f),
                uncheckedThumbColor = Color(0xFF666666),
                uncheckedTrackColor = Color(0xFF444444)
            )
        )
    }
}

@Composable
fun ModernSliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueDisplay: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueDisplay,
                color = Color(0xFF00FF88),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = 5,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00FF88),
                activeTrackColor = Color(0xFF00FF88),
                inactiveTrackColor = Color(0xFF444444)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Быстро", color = Color(0x88FFFFFF), fontSize = 12.sp)
            Text("Медленно", color = Color(0x88FFFFFF), fontSize = 12.sp)
        }
    }
}