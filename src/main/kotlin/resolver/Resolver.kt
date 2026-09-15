package resolver

import model.Player
import model.Position
import model.Team

class Resolver(
    private val players: List<Player>
) : IResolver {


    override fun getCountWithoutAgency(): Int {
        return players.count { it.agency == null }
    }


    override fun getBestScorerDefender(): Pair<String, Int> {
        val player = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?: error("Защитники не найдены")

        return Pair(player.name, player.goals)
    }


    override fun getTheExpensiveGermanPlayerPosition(): String {
        val player = players
            .filter { it.country.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?: error("Немецкие игроки не найдены")

        return player.position.russianName
    }


    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) ->
                teamPlayers.map { it.redCards }.average()
            }
            ?.key
            ?: error("Команды не найдены")
    }


    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) ->
                positionPlayers.map { it.transferCost }.average()
            }
            .toList()
            .sortedByDescending { (_, averageCost) -> averageCost }
            .toMap()
    }

    // 6. Топ-3 самых полезных игроков:
    // голы + 2 * голевые передачи
    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> {
                    it.goals + 2 * it.assists
                }.thenBy {
                    it.name
                }
            )
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .groupBy { it.country }
            .mapNotNull { (country, countryPlayers) ->

                val agencies = countryPlayers
                    .mapNotNull { it.agency }

                if (agencies.isEmpty()) {
                    null
                } else {
                    val mostPopularAgency = agencies
                        .groupingBy { it }
                        .eachCount()
                        .entries
                        .sortedWith(
                            compareByDescending<Map.Entry<String, Int>> {
                                it.value
                            }.thenBy {
                                it.key
                            }
                        )
                        .first()
                        .key

                    country to mostPopularAgency
                }
            }
            .toMap()
    }


    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) {
            return Pair(0.0, 0.0)
        }

        val sortedCosts = players
            .map { it.transferCost }
            .sorted()

        val middle = sortedCosts.size / 2

        val median = if (sortedCosts.size % 2 == 0) {
            (
                    sortedCosts[middle - 1].toDouble() +
                            sortedCosts[middle].toDouble()
                    ) / 2
        } else {
            sortedCosts[middle].toDouble()
        }

        val totalGoals = players.sumOf { it.goals }

        if (totalGoals == 0) {
            return Pair(0.0, 0.0)
        }

        val goalsNotMoreExpensiveThanMedian = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }

        val goalsMoreExpensiveThanMedian = players
            .filter { it.transferCost > median }
            .sumOf { it.goals }

        val firstShare =
            goalsNotMoreExpensiveThanMedian.toDouble() / totalGoals

        val secondShare =
            goalsMoreExpensiveThanMedian.toDouble() / totalGoals

        return Pair(firstShare, secondShare)
    }
}