package resolver

import filter.PlayerFilter
import model.Player
import model.Position
import model.Team

class Resolver(players: List<Player>) : IResolver {
    private val players = players.toList()
    val teams: List<Team> by lazy { Team.aggregate(this.players) }

    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val filter = PlayerFilter.ByPosition(Position.DEFENDER)
        val player = players.filter(filter::matches)
            .sortedWith(compareByDescending<Player> { it.goals }.thenBy { it.name }).firstOrNull()
            ?: throw NoSuchElementException("В выборке нет защитников")
        return player.name to player.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String = players
        .filter { it.country.equals("Germany", ignoreCase = true) }
        .sortedWith(compareByDescending<Player> { it.transferCost }.thenBy { it.name })
        .firstOrNull()?.position?.russianName
        ?: throw NoSuchElementException("В выборке нет немецких игроков")

    override fun getTheRudestTeam(): Team = teams
        .sortedWith(compareByDescending<Team> { it.averageRedCards }.thenBy { it.name }.thenBy { it.city })
        .firstOrNull() ?: throw NoSuchElementException("В выборке нет команд")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> = Position.entries
        .mapNotNull { position ->
            val filter = PlayerFilter.ByPosition(position)
            val matching = players.filter(filter::matches)
            if (matching.isEmpty()) null else position to matching.map { it.transferCost }.average()
        }.sortedByDescending { it.second }.toMap()

    override fun getMostValuablePlayers(): List<Player> = players
        .sortedWith(compareByDescending<Player> { it.goals.toLong() + 2L * it.assists }.thenBy { it.name })
        .take(3)

    // Страны без представленных агентствами игроков отсутствуют в результате.
    override fun getMostPopularAgencyByCountry(): Map<String, String> = players
        .filter { it.agency != null }.groupBy { it.country }.toSortedMap()
        .mapValues { (_, members) ->
            members.groupingBy { requireNotNull(it.agency) }.eachCount().entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .first().key
        }

    fun getMedianTransferCost(): Double {
        if (players.isEmpty()) return 0.0
        val costs = players.map { it.transferCost }.sorted()
        val middle = costs.size / 2
        return if (costs.size % 2 == 1) costs[middle].toDouble()
        else costs[middle - 1] / 2.0 + costs[middle] / 2.0
    }

    // При отсутствии голов обе доли равны нулю.
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val totalGoals = players.sumOf { it.goals.toLong() }
        if (totalGoals == 0L) return 0.0 to 0.0
        val median = getMedianTransferCost()
        val cheaperGoals = players.filter { it.transferCost <= median }.sumOf { it.goals.toLong() }
        return cheaperGoals.toDouble() / totalGoals to (totalGoals - cheaperGoals).toDouble() / totalGoals
    }
}
