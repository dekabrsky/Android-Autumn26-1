package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {
    val teams: List<Team> by lazy {
        players.groupBy { it.teamName }
            .map { (name, teamPlayers) -> Team(name, teamPlayers.first().city, teamPlayers) }
    }

    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val best = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?: throw NoSuchElementException("В выборке нет защитников")
        return best.name to best.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val player = players
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?: throw NoSuchElementException("В выборке нет немецких игроков")
        return player.position.russianName
    }

    override fun getTheRudestTeam(): Team =
        teams.maxByOrNull { it.averageRedCards }
            ?: throw NoSuchElementException("В выборке нет команд")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, positionPlayers) -> positionPlayers.map { it.transferCost }.average() }
            .entries
            .sortedByDescending { it.value }
            .associateTo(LinkedHashMap()) { it.key to it.value }

    override fun getMostValuablePlayers(): List<Player> =
        players
            .sortedWith(compareByDescending<Player> { it.impact }.thenBy { it.name })
            .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players
            .filter { it.agency != null }
            .groupBy { it.nationality }
            .mapValues { (_, countryPlayers) ->
                countryPlayers
                    .groupingBy { it.agency!! }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .first()
                    .key
            }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val median = medianOf(players.map { it.transferCost })
        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0

        val goalsNotAboveMedian = players.filter { it.transferCost <= median }.sumOf { it.goals }
        val goalsAboveMedian = totalGoals - goalsNotAboveMedian

        return goalsNotAboveMedian.toDouble() / totalGoals to goalsAboveMedian.toDouble() / totalGoals
    }

    private fun medianOf(values: List<Long>): Double {
        require(values.isNotEmpty()) { "Список стоимостей пуст" }
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) {
            sorted[middle].toDouble()
        } else {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        }
    }
}
