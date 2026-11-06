package model

data class Team(
    val name: String,
    val city: String,
    val players: List<Player>,
) {
    // Свойства в теле класса не входят в equals/hashCode/toString data-класса.
    val totalTransferCost: Long by lazy { players.sumOf { it.transferCost } }

    val averageCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.sumOf { it.yellowCards + it.redCards }.toDouble() / players.size
    }

    val averageRedCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.sumOf { it.redCards }.toDouble() / players.size
    }
}

/** Собирает команды (команда = название + город) из плоского списка игроков. */
fun List<Player>.toTeams(): List<Team> =
    groupBy { it.teamName to it.city }
        .map { (key, members) -> Team(key.first, key.second, members) }
