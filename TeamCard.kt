package com.example.cs2battlegame.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cs2battlegame.model.Team

@Composable
fun TeamCard(
    team: Team,
    position: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$position. ${team.name} (${team.country})",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Сила команды: ${team.getDisplayStrength()}",
                    style = MaterialTheme.typography.body1
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Победы: ${team.wins}, Поражения: ${team.losses}",
                style = MaterialTheme.typography.body2
            )
        }
    }
}