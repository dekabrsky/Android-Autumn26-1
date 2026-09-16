package resolver

import model.Player
import model.Position
import model.Team

class Resolver(
    private val players: List<Player>
) : IResolver {

    override fun getCountWithoutAgency(): Int {
        return players.count { player ->
            player.agency == null
        }
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        val defenders = players.filter { player ->
            player.position == Position.DEFENDER
        }

        val bestPlayer = defenders.maxByOrNull { player ->
            player.goals
        } ?: error("Защитники не найдены")

        return bestPlayer.name to bestPlayer.goals
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        val germanPlayers = players.filter { player ->
            player.country.equals("Germany", ignoreCase = true)
        }

        val mostExpensive = germanPlayers.maxByOrNull { player ->
            player.transferCost
        } ?: error("Игроки из Германии не найдены")

        return mostExpensive.position.russianName
    }

    override fun getTheRudestTeam(): Team {
        val playersByTeam = players.groupBy { player ->
            player.team
        }

        return playersByTeam
            .maxByOrNull { (_, teamPlayers) ->
                teamPlayers
                    .map { player -> player.redCards }
                    .average()
            }
            ?.key
            ?: error("Команды не найдены")
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        val averageCosts = players
            .groupBy { player -> player.position }
            .mapValues { (_, positionPlayers) ->
                positionPlayers
                    .map { player -> player.transferCost }
                    .average()
            }

        return averageCosts
            .toList()
            .sortedByDescending { (_, averageCost) ->
                averageCost
            }
            .toMap()
    }

    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> { player ->
                    player.goals + 2 * player.assists
                }.thenBy { player ->
                    player.name
                }
            )
            .take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .groupBy { player -> player.country }
            .mapNotNull { (country, countryPlayers) ->

                val agencies = countryPlayers
                    .mapNotNull { player -> player.agency }

                if (agencies.isEmpty()) {
                    return@mapNotNull null
                }

                val popularAgency = agencies
                    .groupingBy { agency -> agency }
                    .eachCount()
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> { entry ->
                            entry.value
                        }.thenBy { entry ->
                            entry.key
                        }
                    )
                    .first()
                    .key

                country to popularAgency
            }
            .toMap()
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) {
            return 0.0 to 0.0
        }

        val costs = players
            .map { player -> player.transferCost }
            .sorted()

        val middleIndex = costs.size / 2

        val medianCost = if (costs.size % 2 == 0) {
            (costs[middleIndex - 1].toDouble() + costs[middleIndex].toDouble()) / 2
        } else {
            costs[middleIndex].toDouble()
        }

        val totalGoals = players.sumOf { player ->
            player.goals
        }

        if (totalGoals == 0) {
            return 0.0 to 0.0
        }

        val goalsAtOrBelowMedian = players
            .filter { player -> player.transferCost <= medianCost }
            .sumOf { player -> player.goals }

        val goalsAboveMedian = players
            .filter { player -> player.transferCost > medianCost }
            .sumOf { player -> player.goals }

        return (
                goalsAtOrBelowMedian.toDouble() / totalGoals
                ) to (
                goalsAboveMedian.toDouble() / totalGoals
                )
    }
}