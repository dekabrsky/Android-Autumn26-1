package Denisova1dz.model

data class Player(
    val name: String,
    val team: Team,
    val position: Position,
    val country: String,
    val agency: String?,
    val transfer: Long,
    val matches: Int,
    val goals : Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int
)
