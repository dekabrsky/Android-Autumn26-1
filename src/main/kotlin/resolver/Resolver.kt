package resolver

import model.Player
import model.Position
import model.Team

class Resolver(players: List<Player>) : IResolver {

    // Player и Team неизменяемы; копируем только структуру входной коллекции.
    private val players: List<Player> = players.toList()

    private val teamAggregates: List<TeamAggregate> =
        players
            .groupBy { it.team.name to it.team.city }
            .map { (_, teamPlayers) ->
                val firstPlayer = teamPlayers.first()

                TeamAggregate(
                    team = Team(
                        name = firstPlayer.team.name,
                        city = firstPlayer.team.city
                    ),
                    players = teamPlayers
                )
            }

    fun teamAggregates(): List<TeamAggregate> = teamAggregates.toList()

    /** Все уникальные команды без состава и статистики. */
    fun allTeams(): List<Team> = teamAggregates.map { it.team }

    override fun countPlayersWithoutAgency(): Int =
        players.count { it.agency == null }

    override fun topScoringDefender(): Pair<Player, Int>? =
        players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?.let { it to it.goals }

    override fun mostExpensiveGermanPlayerPositionName(): String? =
        players
            .filter { it.country.equals("Germany", ignoreCase = true) ||
                    it.country.equals("Германия", ignoreCase = true) }
            .maxByOrNull { it.transferValue }
            ?.position?.russianName

    override fun teamWithHighestAverageRedCards(): Team? =
        teamAggregates.maxByOrNull { it.averageRedCards }?.team

    override fun averageValueByPosition(): List<Pair<Position, Double>> =
        players
            .groupBy { it.position }
            .map { (position, group) ->
                position to group.map { it.transferValue }.average()
            }
            .sortedByDescending { it.second }

    override fun top3ByImpactScore(): List<Player> =
        players
            .sortedWith(
                compareByDescending<Player> { it.impactScore }
                    .thenBy { it.name }
            )
            .take(3)

    override fun mostPopularAgencyByCountry(): Map<String, String?> =
        players
            .groupBy { it.country }
            .mapValues { (_, group) ->
                group
                    .filter { it.agency != null }
                    .groupingBy { it.agency }
                    .eachCount()
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String?, Int>> { it.value }
                            .thenBy { it.key }
                    )
                    .firstOrNull()
                    ?.key
            }

    override fun goalsShareAroundMedianValue(): MedianGoalsReport {
        val sortedByValue = players.sortedBy { it.transferValue }
        val n = sortedByValue.size
        val median = if (n == 0) 0.0 else {
            if (n % 2 == 1) {
                sortedByValue[n / 2].transferValue.toDouble()
            } else {
                val lower = sortedByValue[n / 2 - 1].transferValue
                val upper = sortedByValue[n / 2].transferValue
                (lower + upper) / 2.0
            }
        }

        val totalGoals = players.sumOf { it.goals }
        val goalsBelow = players.filter { it.transferValue < median }.sumOf { it.goals }
        val goalsAbove = players.filter { it.transferValue > median }.sumOf { it.goals }

        val belowPercent = if (totalGoals == 0) 0.0 else goalsBelow * 100.0 / totalGoals
        val abovePercent = if (totalGoals == 0) 0.0 else goalsAbove * 100.0 / totalGoals

        return MedianGoalsReport(median, belowPercent, abovePercent)
    }
}
