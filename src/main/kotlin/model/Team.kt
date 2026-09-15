package model

data class Team(
    val name: String,
    val city: String
) {
    /*
     * players не находится в основном конструкторе, поэтому не участвует
     * в equals/hashCode. Это позволяет сохранить совместимость с тестами,
     * где Team сравнивается только по названию и городу.
     */
    var players: List<Player> = emptyList()
        private set

    val totalTransferCost: Long by lazy {
        players.sumOf { it.transferCost }
    }

    val averageYellowCards: Double by lazy {
        players
            .takeIf { it.isNotEmpty() }
            ?.map { it.yellowCards }
            ?.average()
            ?: 0.0
    }

    val averageRedCards: Double by lazy {
        players
            .takeIf { it.isNotEmpty() }
            ?.map { it.redCards }
            ?.average()
            ?: 0.0
    }

    val averageCards: Double by lazy {
        players
            .takeIf { it.isNotEmpty() }
            ?.map { it.yellowCards + it.redCards }
            ?.average()
            ?: 0.0
    }

    companion object {
        fun aggregate(players: List<Player>): List<Team> =
            players
                .groupBy { player ->
                    player.team.name to player.team.city
                }
                .map { (teamIdentity, teamPlayers) ->
                    Team(
                        name = teamIdentity.first,
                        city = teamIdentity.second
                    ).apply {
                        this.players = teamPlayers
                    }
                }
    }
}