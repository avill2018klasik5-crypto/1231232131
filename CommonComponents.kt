package com.example.cs2battlegame.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernBackButton(
    onClick: () -> Unit,
    text: String
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
        elevation = if (isHovered) 8.dp else 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable(onClick = onClick)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6200EE).copy(alpha = 0.3f),
                            Color(0xFF3700B3).copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "←",
                    color = Color(0xFFBB86FC),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}