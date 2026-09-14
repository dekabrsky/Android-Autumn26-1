package model

data class Player(
    val Name: String,
    val Team: Team,
    val Position: Position,
    val Nationality: String,
    val Agency: String?,
    val TransferCost: Long,
    val Participations: Int,
    val Goals: Int,
    val Assists: Int,
    val Yellow_cards: Int,
    val Red_cards: Int
)
