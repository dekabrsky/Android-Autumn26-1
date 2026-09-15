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

    override fun getBestScorerDefender(): Pair<String, Int> =
        players.asSequence()
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?.let { it.name to it.goals }
            ?: throw NoSuchElementException("В выборке нет защитников")

    override fun getTheExpensiveGermanPlayerPosition(): String =
        players.asSequence()
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?.position?.russianName
            ?: throw NoSuchElementException("В выборке нет немецких игроков")

    override fun getTheRudestTeam(): Team =
        teams.maxByOrNull { it.averageRedCards }
            ?: throw NoSuchElementException("В выборке нет команд")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, positionPlayers) -> positionPlayers.map { it.transferCost }.average() }
            .toList()
            .sortedByDescending { (_, average) -> average }
            .toMap()

    override fun getMostValuablePlayers(): List<Player> =
        players.sortedWith(compareByDescending<Player> { it.impact }.thenBy { it.name })
            .take(3)

    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.groupBy { it.nationality }
            .mapValues { (_, countryPlayers) ->
                countryPlayers.mapNotNull { it.agency }
                    .groupingBy { it }
                    .eachCount()
                    .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenByDescending { it.key })
                    ?.key ?: "Unknown"
            }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0

        val median = medianOf(players.map { it.transferCost })
        val goalsNotAboveMedian = players.filter { it.transferCost <= median }.sumOf { it.goals }

        val shareNotAbove = goalsNotAboveMedian.toDouble() / totalGoals
        return shareNotAbove to (1.0 - shareNotAbove)
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