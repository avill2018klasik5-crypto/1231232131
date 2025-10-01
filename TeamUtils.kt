package com.example.cs2battlegame.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.cs2battlegame.R
import android.util.Log

@Composable
fun TeamLogo(teamName: String, modifier: Modifier = Modifier) {
    SafeTeamLogo(teamName, modifier)
}

@Composable
fun SafeTeamLogo(teamName: String, modifier: Modifier = Modifier) {
    val logoInfo = getLogoResourceInfoWithFallback(teamName)

    Box(modifier = modifier) {
        if (logoInfo.isValid) {
            Image(
                painter = painterResource(id = logoInfo.resId),
                contentDescription = logoInfo.description,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // Показываем текстовый логотип при ошибке
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = logoInfo.fallbackText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class LogoInfo(
    val resId: Int,
    val description: String,
    val fallbackText: String,
    val isValid: Boolean
)

private fun getLogoResourceInfoWithFallback(teamName: String): LogoInfo {
    return try {
        val (resId, desc) = getLogoResourceInfo(teamName)
        LogoInfo(resId, desc, teamName.take(2).uppercase(), true)
    } catch (e: Exception) {
        Log.e("TeamLogo", "Ошибка получения ресурса для $teamName: ${e.message}")
        LogoInfo(R.drawable.logo_default, "Логотип по умолчанию", teamName.take(2).uppercase(), false)
    }
}

private fun getLogoResourceInfo(teamName: String): Pair<Int, String> {
    return when (teamName) {
        "paIN" -> Pair(R.drawable.logo_pain, "Логотип paIN")
        "Lynn Vision" -> Pair(R.drawable.logo_lynn, "Логотип Lynn Vision")
        "B8" -> Pair(R.drawable.logo_b8, "Логотип B8")
        "Ninjas in Pyjamas" -> Pair(R.drawable.logo_nip, "Логотип NIP")
        "fnatic" -> Pair(R.drawable.logo_fnatic, "Логотип fnatic")
        "Gentle Mates" -> Pair(R.drawable.logo_gm, "Логотип Gantle Mates")
        "9INE" -> Pair(R.drawable.logo_9ine, "Логотип 9INE")
        "Passion UA" -> Pair(R.drawable.logo_ua, "Логотип Passion UA")
        "SAW" -> Pair(R.drawable.logo_saw, "Логотип SAW")
        "BetBoom" -> Pair(R.drawable.logo_bb, "Логотип BetBoom")
        "PARIVISION" -> Pair(R.drawable.logo_parivision, "Логотип PARIVISION")
        "OG" -> Pair(R.drawable.logo_og, "Логотип OG")
        "NAVI" -> Pair(R.drawable.logo_navi, "Логотип NAVI")
        "Vitality" -> Pair(R.drawable.logo_vitality, "Логотип Vitality")
        "Faze Clan" -> Pair(R.drawable.logo_faze, "Логотип Faze Clan")
        "G2 Esports" -> Pair(R.drawable.logo_g2, "Логотип G2 Esports")
        "Astralis" -> Pair(R.drawable.logo_astralis, "Логотип Astralis")
        "Virtus.pro" -> Pair(R.drawable.logo_virtuspro, "Логотип Virtus.pro")
        "Team Spirit" -> Pair(R.drawable.logo_teamspirit, "Логотип Team Spirit")
        "Aurora" -> Pair(R.drawable.logo_aurora, "Логотип Aurora")
        "Falcons" -> Pair(R.drawable.logo_falcons, "Логотип Falcons")
        "3DMAX" -> Pair(R.drawable.logo_3dmax, "Логотип 3DMAX")
        "TYLOO" -> Pair(R.drawable.logo_tyloo, "Логотип TYLOO")
        "GamerLegion" -> Pair(R.drawable.logo_gamerlegion, "Логотип GamerLegion")
        "Legacy" -> Pair(R.drawable.logo_legacy, "Логотип Legacy")
        "M80" -> Pair(R.drawable.logo_m80, "Логотип M80")
        "Heroic" -> Pair(R.drawable.logo_heroic, "Логотип Heroic")
        "Liquid" -> Pair(R.drawable.logo_liquid, "Логотип Liquid")
        "MOUZ" -> Pair(R.drawable.logo_mouz, "Логотип MOUZ")
        "ENCE" -> Pair(R.drawable.logo_ence, "Логотип ENCE")
        "The MongolZ" -> Pair(R.drawable.logo_themongolz, "The MongolZ")
        "FURIA" -> Pair(R.drawable.logo_furia, "Логотип FURIA")
        else -> {
            Log.w("TeamLogo", "Логотип для команды $teamName не найден, используется дефолтный")
            Pair(R.drawable.logo_default, "Логотип по умолчанию")
        }
    }
}