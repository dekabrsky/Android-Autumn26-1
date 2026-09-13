package model

data class Team(
    val name: String,
    val city: String,
    val players: List<Player> = emptyList()
) {
    // суммарная стоимость состава — считаем один раз при первом обращении
    val totalTransferCost: Long by lazy {
        players.sumOf { it.transferCost }
    }

    // среднее число красных карточек на игрока
    val averageRedCards: Double by lazy {
        if (players.isEmpty()) 0.0
        else players.sumOf { it.redCards }.toDouble() / players.size
    }

    // среднее число всех карточек (жёлтых + красных)
    val averageCards: Double by lazy {
        if (players.isEmpty()) 0.0
        else players.sumOf { it.yellowCards + it.redCards }.toDouble() / players.size
    }
}
