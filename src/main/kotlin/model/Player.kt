package model

data class Player(
    val name: String,
    val team: Team,
    val position: Position,
    val country: String,
    val agency: String?,
    val transferCost: Long,
    val participations: Int,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int
) {
    val usefulness: Int
        get() = goals + 2 * assists
}
