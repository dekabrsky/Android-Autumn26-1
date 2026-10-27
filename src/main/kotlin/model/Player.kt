package model

data class Player(
    val name: String,
    val team: Team,
    val position: Position,
    val country: String,
    val agency: String?,
    val transferValue: Long,
    val matchesPlayed: Int,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int
) {
    /** Ценность по формуле "голы + 2 × передачи", используется в задаче 6. */
    val impactScore: Int get() = goals + 2 * assists
}
