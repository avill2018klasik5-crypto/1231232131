// MapPool.kt
package com.example.cs2battlegame.model

enum class MapPool {
    MIRAGE, INFERNO, DUST2, OVERPASS, VERTIGO, ANCIENT, NUKE, ANUBIS, TRAIN;

    fun getDisplayName(): String {
        return when (this) {
            MIRAGE -> "Mirage"
            INFERNO -> "Inferno"
            DUST2 -> "Dust II"
            OVERPASS -> "Overpass"
            VERTIGO -> "Vertigo"
            ANCIENT -> "Ancient"
            NUKE -> "Nuke"
            ANUBIS -> "Anubis"
            TRAIN -> "Train"
        }
    }

    companion object {
        fun getDisplayNameFromString(mapName: String): String {
            return when (mapName.uppercase()) {
                "MIRAGE" -> "Mirage"
                "INFERNO" -> "Inferno"
                "DUST2", "DUST II" -> "Dust II"
                "OVERPASS" -> "Overpass"
                "VERTIGO" -> "Vertigo"
                "ANCIENT" -> "Ancient"
                "NUKE" -> "Nuke"
                "ANUBIS" -> "Anubis"
                "TRAIN" -> "Train"
                else -> mapName
            }
        }
    }
}