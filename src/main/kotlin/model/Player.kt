package model

data class Player(
    val name: String,
    val teamName: String,
    val city: String,
    val position: Position,
    val country: String,
    /** null — интересы игрока никто не представляет (часть предметной области). */
    val agency: String?,
    val transferCost: Long,
    val appearances: Int,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
) {
    /** Полезность игрока: голы + 2 * голевые передачи. */
    val utility: Int get() = goals + 2 * assists
}
