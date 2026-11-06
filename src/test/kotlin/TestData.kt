import model.Player
import model.Position

fun player(
    name: String = "Test",
    team: String = "Reds",
    city: String = "Rome",
    position: Position = Position.FORWARD,
    country: String = "Spain",
    agency: String? = "Alpha",
    cost: Long = 100,
    appearances: Int = 10,
    goals: Int = 0,
    assists: Int = 0,
    yellow: Int = 0,
    red: Int = 0,
) = Player(name, team, city, position, country, agency, cost, appearances, goals, assists, yellow, red)
