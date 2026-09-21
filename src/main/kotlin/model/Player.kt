package model

data class Player(
    val name: String,
    val team: Team,
    val position: Position,
    val country: String,
    val agency: String?,
    val transferCost: Int,
    val gamesCount: Int,
    val goalsCount: Int,
    val goalPassesCount: Int,
    val yellowCardsCount: Int,
    val redCardsCount: Int,
) {
    init {
        team.register(this)
    }
}
