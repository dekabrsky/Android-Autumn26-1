import model.Player
import model.Position
import model.Team

/**
 * Небольшой набор игроков, на котором удобно проверять запросы:
 * все ответы считаются руками.
 */
object TestPlayers {

    val REDS = Team(name = "Reds", city = "Rome")
    val BLUES = Team(name = "Blues", city = "Paris")

    val ALPHA = player(
        name = "Alpha", team = REDS, position = Position.DEFENDER, country = "Germany",
        agency = "Agency A", transferCost = 100, goals = 10, assists = 1, redCards = 4,
    )

    val BRAVO = player(
        name = "Bravo", team = REDS, position = Position.DEFENDER, country = "Germany",
        agency = null, transferCost = 300, goals = 10, assists = 1, redCards = 0,
    )

    val CHARLIE = player(
        name = "Charlie", team = BLUES, position = Position.FORWARD, country = "Brazil",
        agency = "Agency A", transferCost = 250, goals = 5, assists = 7, redCards = 6,
    )

    val DELTA = player(
        name = "Delta", team = BLUES, position = Position.GOALKEEPER, country = "Brazil",
        agency = "Agency B", transferCost = 400, goals = 0, assists = 0, redCards = 2,
    )

    val ECHO = player(
        name = "Echo", team = BLUES, position = Position.MIDFIELD, country = "Spain",
        agency = null, transferCost = 500, goals = 15, assists = 0, redCards = 0,
    )

    val ALL = listOf(ALPHA, BRAVO, CHARLIE, DELTA, ECHO)

    fun player(
        name: String,
        team: Team = REDS,
        position: Position = Position.FORWARD,
        country: String = "Germany",
        agency: String? = "Agency A",
        transferCost: Long = 1_000,
        participations: Int = 20,
        goals: Int = 0,
        assists: Int = 0,
        yellowCards: Int = 0,
        redCards: Int = 0,
    ) = Player(
        name = name,
        team = team,
        position = position,
        country = country,
        agency = agency,
        transferCost = transferCost,
        participations = participations,
        goals = goals,
        assists = assists,
        yellowCards = yellowCards,
        redCards = redCards,
    )
}
