package model

// Состав агрегата содержит игроков с обычной ссылкой на команду, без циклических списков.
data class Team(val name: String, val city: String) {
    var players: List<Player> = emptyList()
        private set

    val totalTransferCost: Long by lazy { players.sumOf { it.transferCost } }
    val averageYellowCards: Double by lazy { players.map { it.yellowCards }.averageOrZero() }
    val averageRedCards: Double by lazy { players.map { it.redCards }.averageOrZero() }

    companion object {
        fun aggregate(players: List<Player>): List<Team> =
            players.groupBy { it.team }.map { (team, members) ->
                Team(team.name, team.city).apply { this.players = members.toList() }
            }
    }
}

private fun List<Int>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()
