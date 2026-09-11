package model

data class Team(
    val name: String,
    val city: String,
    var numberOfPlayers: Int = 0
)