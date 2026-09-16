package resolver

import model.Player
import model.Position
import model.Team
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolverTest {

    private fun p(
        name: String,
        teamName: String = "Team",
        teamCity: String = "City",
        position: Position = Position.FORWARD,
        country: String = "Country",
        agency: String? = "Agency",
        transferValue: Long = 1_000L,
        matches: Int = 10,
        goals: Int = 0,
        assists: Int = 0,
        yellowCards: Int = 0,
        redCards: Int = 0
    ) = Player(
        name = name,
        team = Team(teamName, teamCity),
        position = position,
        country = country,
        agency = agency,
        transferValue = transferValue,
        matches = matches,
        goals = goals,
        assists = assists,
        yellowCards = yellowCards,
        redCards = redCards
    )

    // 1 getCountWithoutAgency

    @Test
    fun `getCountWithoutAgency counts null agencies only`() {
        val r = Resolver(listOf(
            p("A", agency = null),
            p("B", agency = "X"),
            p("C", agency = null)
        ))
        assertEquals(2, r.getCountWithoutAgency())
    }

    @Test
    fun `getCountWithoutAgency returns 0 when all have agencies`() {
        val r = Resolver(listOf(p("A", agency = "X"), p("B", agency = "Y")))
        assertEquals(0, r.getCountWithoutAgency())
    }

    @Test
    fun `getCountWithoutAgency returns 0 for empty list`() {
        assertEquals(0, Resolver(emptyList()).getCountWithoutAgency())
    }

    // 2 getBestScorerDefender

    @Test
    fun `getBestScorerDefender picks top-scoring defender`() {
        val r = Resolver(listOf(
            p("A", position = Position.DEFENDER, goals = 3),
            p("B", position = Position.DEFENDER, goals = 7),
            p("C", position = Position.FORWARD,  goals = 99)
        ))
        assertEquals("B" to 7, r.getBestScorerDefender())
    }

    @Test
    fun `getBestScorerDefender breaks ties alphabetically`() {
        val r = Resolver(listOf(
            p("Zed",  position = Position.DEFENDER, goals = 5),
            p("Adam", position = Position.DEFENDER, goals = 5),
            p("Bob",  position = Position.DEFENDER, goals = 5)
        ))
        assertEquals("Adam" to 5, r.getBestScorerDefender())
    }

    @Test
    fun `getBestScorerDefender works with 2 defenders`() {
        val r = Resolver(listOf(
            p("Solo", position = Position.DEFENDER, goals = 1),
            p("Other", position = Position.DEFENDER, goals = 4)
        ))
        assertEquals("Other" to 4, r.getBestScorerDefender())
    }

    // 3 getTheExpensiveGermanPlayerPosition

    @Test
    fun `getTheExpensiveGermanPlayerPosition returns ru name of top German`() {
        val r = Resolver(listOf(
            p("A", position = Position.MIDFIELDER, country = "Germany", transferValue = 500),
            p("B", position = Position.FORWARD,    country = "Germany", transferValue = 900),
            p("C", position = Position.GOALKEEPER, country = "France",  transferValue = 9999)
        ))
        assertEquals("нападающий", r.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `getTheExpensiveGermanPlayerPosition is case-insensitive on country`() {
        val r = Resolver(listOf(
            p("A", country = "germany", position = Position.DEFENDER, transferValue = 100),
            p("B", country = "GERMANY", position = Position.FORWARD,  transferValue = 200)
        ))
        assertEquals("нападающий", r.getTheExpensiveGermanPlayerPosition())
    }

    @Test
    fun `getTheExpensiveGermanPlayerPosition with only one German`() {
        val r = Resolver(listOf(
            p("A", country = "Germany", position = Position.DEFENDER, transferValue = 100),
            p("B", country = "France",  position = Position.FORWARD,  transferValue = 9999)
        ))
        assertEquals("защитник", r.getTheExpensiveGermanPlayerPosition())
    }

    // 4 getTheRudestTeam

    @Test
    fun `getTheRudestTeam picks highest average red cards`() {
        val r = Resolver(listOf(
            p("A", teamName = "Alpha", teamCity = "X", redCards = 5),
            p("B", teamName = "Alpha", teamCity = "X", redCards = 1),
            p("C", teamName = "Beta",  teamCity = "Y", redCards = 10)
        ))
        assertEquals(Team("Beta", "Y"), r.getTheRudestTeam())
    }

    @Test
    fun `getTheRudestTeam treats same name different city as different teams`() {
        val r = Resolver(listOf(
            p("A", teamName = "United", teamCity = "London", redCards = 1),
            p("B", teamName = "United", teamCity = "London", redCards = 1),
            p("C", teamName = "United", teamCity = "Paris",  redCards = 9)
        ))
        assertEquals(Team("United", "Paris"), r.getTheRudestTeam())
    }

    // 5 getAverageTransferCostByPosition

    @Test
    fun `getAverageTransferCostByPosition averages and sorts descending`() {
        val r = Resolver(listOf(
            p("A", position = Position.DEFENDER,   transferValue = 100),
            p("B", position = Position.DEFENDER,   transferValue = 300),
            p("C", position = Position.FORWARD,    transferValue = 500),
            p("D", position = Position.FORWARD,    transferValue = 700),
            p("E", position = Position.MIDFIELDER, transferValue = 400)
        ))
        val map = r.getAverageTransferCostByPosition()

        assertEquals(
            listOf(Position.FORWARD, Position.MIDFIELDER, Position.DEFENDER),
            map.keys.toList(),
            "Позиции должны идти по убыванию средней стоимости"
        )
        assertEquals(600.0, map[Position.FORWARD]!!, 1e-9)
        assertEquals(400.0, map[Position.MIDFIELDER]!!, 1e-9)
        assertEquals(200.0, map[Position.DEFENDER]!!, 1e-9)
    }

    @Test
    fun `getAverageTransferCostByPosition handles single player`() {
        val r = Resolver(listOf(p("A", position = Position.GOALKEEPER, transferValue = 42)))
        assertEquals(mapOf(Position.GOALKEEPER to 42.0), r.getAverageTransferCostByPosition())
    }

    // 6 getMostValuablePlayers

    @Test
    fun `getMostValuablePlayers uses goals + 2 assists formula`() {
        val r = Resolver(listOf(
            p("A", goals = 10, assists = 0),
            p("B", goals = 0,  assists = 10),
            p("C", goals = 5,  assists = 5),
            p("D", goals = 1,  assists = 1)
        ))
        assertEquals(
            listOf("B", "C", "A"),
            r.getMostValuablePlayers().map { it.name }
        )
    }

    @Test
    fun `getMostValuablePlayers breaks ties alphabetically`() {
        val r = Resolver(listOf(
            p("Zed",  goals = 5, assists = 5),
            p("Adam", goals = 5, assists = 5),
            p("Bob",  goals = 5, assists = 5),
            p("Cara", goals = 1, assists = 1)
        ))
        assertEquals(
            listOf("Adam", "Bob", "Zed"),
            r.getMostValuablePlayers().map { it.name }
        )
    }

    @Test
    fun `getMostValuablePlayers returns at most 3`() {
        val r = Resolver(listOf(
            p("A", goals = 10),
            p("B", goals = 9),
            p("C", goals = 8),
            p("D", goals = 7),
            p("E", goals = 6)
        ))
        assertEquals(3, r.getMostValuablePlayers().size)
    }

    // 7 getMostPopularAgencyByCountry

    @Test
    fun `getMostPopularAgencyByCountry returns mode per country`() {
        val r = Resolver(listOf(
            p("A", country = "Italy", agency = "Zeta"),
            p("B", country = "Italy", agency = "Alpha"),
            p("C", country = "Italy", agency = "Alpha"),
            p("D", country = "Italy", agency = "Zeta"),
            p("E", country = "Italy", agency = "Zeta")
        ))
        assertEquals(mapOf("Italy" to "Zeta"), r.getMostPopularAgencyByCountry())
    }

    @Test
    fun `getMostPopularAgencyByCountry breaks frequency ties alphabetically`() {
        val r = Resolver(listOf(
            p("A", country = "Italy", agency = "Zeta"),
            p("B", country = "Italy", agency = "Alpha"),
            p("C", country = "Italy", agency = "Beta"),
            p("D", country = "Italy", agency = "Alpha")
        ))
        assertEquals(mapOf("Italy" to "Alpha"), r.getMostPopularAgencyByCountry())
    }

    @Test
    fun `getMostPopularAgencyByCountry ignores players without agency`() {
        val r = Resolver(listOf(
            p("A", country = "Spain", agency = null),
            p("B", country = "Spain", agency = null)
        ))
        assertEquals(emptyMap<String, String>(), r.getMostPopularAgencyByCountry())
    }

    @Test
    fun `getMostPopularAgencyByCountry handles multiple countries`() {
        val r = Resolver(listOf(
            p("A", country = "Italy",  agency = "Alpha"),
            p("B", country = "Italy",  agency = "Alpha"),
            p("C", country = "France", agency = "Beta"),
            p("D", country = "France", agency = "Gamma")
        ))
        val map = r.getMostPopularAgencyByCountry()
        assertEquals("Alpha", map["Italy"])
        assertEquals("Beta",  map["France"])
    }

    // 8 getGoalsShareByMedianCost

    @Test
    fun `getGoalsShareByMedianCost even-sized dataset`() {
        val r = Resolver(listOf(
            p("A", transferValue = 10, goals = 1),
            p("B", transferValue = 20, goals = 2),
            p("C", transferValue = 30, goals = 3),
            p("D", transferValue = 40, goals = 4)
        ))
        val (below, above) = r.getGoalsShareByMedianCost()
        assertEquals(0.3, below, 1e-9)
        assertEquals(0.7, above, 1e-9)
        assertEquals(1.0, below + above, 1e-9)
    }

    @Test
    fun `getGoalsShareByMedianCost odd-sized dataset`() {
        val r = Resolver(listOf(
            p("A", transferValue = 10, goals = 1),
            p("B", transferValue = 20, goals = 2),
            p("C", transferValue = 30, goals = 4)
        ))
        val (below, above) = r.getGoalsShareByMedianCost()
        assertEquals(3.0 / 7.0, below, 1e-9)
        assertEquals(4.0 / 7.0, above, 1e-9)
    }

    @Test
    fun `getGoalsShareByMedianCost with all zero goals returns zeros`() {
        val r = Resolver(listOf(
            p("A", transferValue = 10, goals = 0),
            p("B", transferValue = 20, goals = 0)
        ))
        assertEquals(0.0 to 0.0, r.getGoalsShareByMedianCost())
    }

    @Test
    fun `getGoalsShareByMedianCost with empty list returns zeros`() {
        assertEquals(0.0 to 0.0, Resolver(emptyList()).getGoalsShareByMedianCost())
    }

    @Test
    fun `getGoalsShareByMedianCost when all costs equal - all goals below`() {
        // median = 100; все игроки <= median -> below = 1.0, above = 0.0
        val r = Resolver(listOf(
            p("A", transferValue = 100, goals = 2),
            p("B", transferValue = 100, goals = 3)
        ))
        val (below, above) = r.getGoalsShareByMedianCost()
        assertEquals(1.0, below, 1e-9)
        assertEquals(0.0, above, 1e-9)
    }
}