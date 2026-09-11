package resolver

import filter.ByPosition
import filter.CheaperThan
import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defenderFilter = ByPosition(Position.DEFENDER)
        val bestScorer = players
            .filter(defenderFilter::matches)
            .maxByOrNull { it.goals }
            ?: error("Нет защитников")
        return bestScorer.name to bestScorer.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val mostExpensiveGerman = players
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?: error("Нет немецких игроков")
        return mostExpensiveGerman.position.russianName
    }

    override fun getTheRudestTeam(): Team {
        val teams = players.groupBy { it.team }.map { (team, teamPlayers) ->
            Team(team.name, team.city, teamPlayers)
        }
        return teams.maxByOrNull { it.averageRedCards } ?: error("Нет команд")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> = players
        .groupBy(Player::position)
        .map { (position, positionPlayers) ->
            position to positionPlayers.map(Player::transferCost).average()
        }
        .sortedByDescending { it.second }
        .toMap(linkedMapOf())

    override fun getMostValuablePlayers(): List<Player> = players
        .sortedWith(
            compareByDescending<Player> { it.goals.toLong() + 2L * it.assists }
                .thenBy(Player::name),
        )
        .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players.groupBy { it.nationality }.mapValues { (_, countryPlayers) ->
            countryPlayers.mapNotNull { it.agency }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .first()
                .key
        }
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        require(players.isNotEmpty()) { "Невозможно посчитать медиану" }
        val sortedCosts = players.map(Player::transferCost).sorted()
        val middleIndex = sortedCosts.size / 2
        val median = if (sortedCosts.size % 2 == 0) {
            (sortedCosts[middleIndex - 1].toDouble() + sortedCosts[middleIndex]) / 2.0
        } else {
            sortedCosts[middleIndex].toDouble()
        }
        val totalGoals = players.sumOf { it.goals.toLong() }
        if (totalGoals == 0L) return 0.0 to 0.0
        val notMoreExpensiveThanMedian = CheaperThan(median.toLong() + 1)
        val cheaperPlayersGoals = players
            .asSequence()
            .filter(notMoreExpensiveThanMedian::matches)
            .sumOf { it.goals.toLong() }
        val expensivePlayersGoals = totalGoals - cheaperPlayersGoals
        return cheaperPlayersGoals.toDouble() / totalGoals to
            expensivePlayersGoals.toDouble() / totalGoals
    }
}
