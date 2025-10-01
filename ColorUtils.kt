package com.example.cs2battlegame.utils

import androidx.compose.ui.graphics.Color

object ColorUtils {

    fun getTeamPrimaryColor(teamName: String): Color {
        return when (teamName) {
            "Vitality" -> Color(0xFFFFD700) // желтый
            "Team Spirit" -> Color(0xFFFFFFFF) // чёрный
            "MOUZ" -> Color(0xFFFF0000) // красный
            "G2 Esports" -> Color(0xFFFFFFFF) // белый
            "Falcons" -> Color(0xFF00FF00) // зелёный
            "Faze Clan" -> Color(0xFFFF0000) // красный
            "Heroic" -> Color(0xFFFFFFFF) // белый
            "FURIA" -> Color(0xFF000000) // чёрный
            "Aurora" -> Color(0xFF00BFFF) // голубой
            "Astralis" -> Color(0xFFFF0000) // красный
            "Legacy" -> Color(0xFFFFD700) // жёлтый
            "M80" -> Color(0xFFFFD700) // жёлтый
            "NAVI" -> Color(0xFFFFD700) // жёлтый
            "paiN " -> Color(0xFF000000) // чёрный
            "3DMAX" -> Color(0xFFFF0000) // красный
            "TYLOO" -> Color(0xFFFF0000) // красный
            "GamerLegion" -> Color(0xFF000000) // чёрный
            "Virtus.pro" -> Color(0xFFFFA500) // белый
            "Liquid" -> Color(0xFF0000FF) // Синий
            "ENCE" -> Color(0xFF8B4513) // коричневый
            "OG" -> Color(0xFF0000FF) // синий
            "BetBoom" -> Color(0xFFDC143C) // малиновый
            "PARIVISION" -> Color(0xFF000000) // голубой
            "The MongolZ" -> Color(0xFFFFD700) // жёлтый
            "Lynn Vision" -> Color(0xFFFFA500) // оранжевый
            "B8" -> Color(0xFF000000) // чёрный
            "Ninjas in Pyjamas" -> Color(0xFF006400) // тёмно-зеленый
            "fnatic" -> Color(0xFF000000) // чёрный
            "Gentle Mates" -> Color(0xFFFFFFFF) // чёрный
            "9INE" -> Color(0xFF000000) // чёрный
            "Passion UA" -> Color(0xFF000000) // белый
            "SAW" -> Color(0xFF000000) // чёрный
            else -> Color(0xFF666666) // серый по умолчанию
        }
    }

    fun getTeamScoreBackgroundColor(teamName: String): Color {
        return when (teamName) {
            "The MongolZ" -> Color(0xFFFFA500) // оранжевый
            "Vitality" -> Color(0xFFB8860B) // тёмно-жёлтый
            "Team Spirit" -> Color(0xFF444444) // серый
            "MOUZ" -> Color(0xFF8B0000) // тёмно-красный
            "G2 Esports" -> Color(0xFF888888) // тёмно-серый
            "Falcons" -> Color(0xFF006400) // тёмно-зелёный
            "Faze Clan" -> Color(0xFF8B0000) // тёмно-красный
            "Heroic" -> Color(0xFF888888) // серый
            "FURIA" -> Color(0xFF444444) // серый
            "Aurora" -> Color(0xFF0000FF) // синий
            "Astralis" -> Color(0xFF8B0000) // тёмно-красный
            "Legacy" -> Color(0xFFB8860B) // тёмно-желтый
            "M80" -> Color(0xFF000000) // чёрный
            "NAVI" -> Color(0xFF000000) // чёрный
            "paiN Gaming" -> Color(0xFFFF0000) // красный
            "3DMAX" -> Color(0xFF8B0000) // тёмно-красный
            "TYLOO" -> Color(0xFF000000) // чёрный
            "GamerLegion" -> Color(0xFF87CEEB) // небесно-голубой
            "Virtus.pro" -> Color(0xFFFFA500) // оранжевый
            "Liquid" -> Color(0xFF000080) // тёмно-синий
            "ENCE" -> Color(0xFF000000) // чёрный
            "OG" -> Color(0xFFFFFFFF) // белый
            "BetBoom" -> Color(0xFF8B0000) // тёмно-красный
            "paiN" -> Color(0xFFFF0000) // красный
            "Lynn Vision" -> Color(0xFFB8860B) // жёлтый
            "B8" -> Color(0xFF444444) // серый
            "Ninjas in Pyjamas" -> Color(0xFF000000) // чёрный
            "fnatic" -> Color(0xFF888888) // серый
            "Gentle Mates" -> Color(0xFF888888) // серый
            "9INE" -> Color(0xFF888888) // серый
            "Passion UA" -> Color(0xFFFFD700) // синий
            "SAW" -> Color(0xFF000000) // чёрный
            "PARIVISION" -> Color(0xFF87CEEB) // голубой
            else -> Color(0xFF333333) // тёмно-серый по умолчанию
        }
    }

    fun getTeamBackgroundColor(teamName: String): Color {
        return getTeamPrimaryColor(teamName)
    }

    fun getContrastTextColor(backgroundColor: Color): Color {
        val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
        return if (luminance > 0.5) Color.Black else Color.White
    }
}
