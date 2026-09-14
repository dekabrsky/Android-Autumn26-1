import model.Player
import model.Position
import model.Team

// Фабрика игроков для тестов: задаём только значимые для проверки поля.
fun player(
    name: String,
    team: Team = Team("Test team", "Test city"),
    position: Position = Position.MIDFIELD,
    nationality: String = "Brazil",
    agency: String? = "Agency",
    transferCost: Long = 1_000_000,
    participations: Int = 10,
    goals: Int = 0,
    assists: Int = 0,
    yellowCards: Int = 0,
    redCards: Int = 0,
) = Player(
    name = name,
    team = team,
    position = position,
    nationality = nationality,
    agency = agency,
    transferCost = transferCost,
    participations = participations,
    goals = goals,
    assists = assists,
    yellowCards = yellowCards,
    redCards = redCards,
)
