package model

data class ParseResult(
    val players: List<Player>,
    val skippedCount: Int
)