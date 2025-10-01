package com.example.cs2battlegame.model

enum class PlayerRole {
    AWPER,
    ENTRY,
    SUPPORT,
    LURKER,
    IGL
}

data class Player(
    val name: String,
    val role: PlayerRole,
    var skill: Int,
    var health: Int = 100,
    var kills: Int = 0,
    var deaths: Int = 0,
    var assists: Int = 0
) {
    fun isAlive(): Boolean = health > 0

    fun takeDamage(damage: Int) {
        health = maxOf(0, health - damage)
        if (health == 0) {
            deaths++
        }
    }

    fun addKill() {
        kills++
    }

    fun addDeath() {
        deaths++
    }

    fun addAssist() {
        assists++
    }

    fun resetStats() {
        health = 100
        kills = 0
        deaths = 0
        assists = 0
    }

    fun getKDARatio(): String {
        val kd = if (deaths > 0) kills.toFloat() / deaths else kills.toFloat()
        return "%.2f".format(kd)
    }
}