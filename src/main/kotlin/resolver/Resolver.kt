package resolver

import model.Player
import model.Position
import model.Team
import parser.CsvParser

class Resolver(private val allPlayers: List<Player> = CsvParser.parse().players) : IResolver {

    override fun getCountWithoutAgency(): Int {
        var count = 0
        for (player in allPlayers) {
            if (player.agency == null) {
                count++
            }
        }
        return count
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        var bestName = ""
        var maxGoals = -1
        
        for (player in allPlayers) {
            if (player.position == Position.DEFENDER) {
                if (player.goals > maxGoals) {
                    maxGoals = player.goals
                    bestName = player.name
                }
            }
        }
        return Pair(bestName, maxGoals)
    }

    override fun getTheExpensiveGermanPlayerPosition(): String {
        var maxCost = -1L
        var resultPosition = ""
        
        for (player in allPlayers) {
            if (player.nationality == "Germany") {
                if (player.transferCost > maxCost) {
                    maxCost = player.transferCost
                    resultPosition = player.position.russianName
                }
            }
        }
        return resultPosition
    }

    override fun getTheRudestTeam(): Team {
        val teams = mutableSetOf<Team>()
        for (player in allPlayers) {
            teams.add(player.team)
        }

        var rudest: Team? = null
        var maxAverage = -1.0
        
        for (team in teams) {
            if (team.averageRedCards > maxAverage) {
                maxAverage = team.averageRedCards
                rudest = team
            }
        }
        
        return rudest!!
    }

    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        val stats = mutableMapOf<Position, Pair<Int, Long>>()
        for (player in allPlayers) {
            val position = player.position
            val current = stats[position]

            if (current == null)
                stats[position] = Pair(1, player.transferCost)
            else {
                val newCount = current.first + 1
                val newCost = current.second + player.transferCost
                stats[position] = Pair(newCount, newCost)
            }
        }

        val result = mutableMapOf<Position, Double>()
        for (entry in stats) {
            val position = entry.key
            val count = entry.value.first
            val cost = entry.value.second

            result[position] = cost.toDouble() / count
        }
        return result.entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
    }

    override fun getMostValuablePlayers(): List<Player> {
        val players = allPlayers.sortedWith(compareByDescending<Player> { it.goals + 2 * it.assists }
            .thenBy { it.name })
        return players.take(3)
    }

    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        val countryStats = mutableMapOf<String, MutableMap<String, Int>>()

        for (player in allPlayers) {
            val agency = player.agency ?: continue
            val country = player.nationality
            val agencyCounts = countryStats.getOrPut(country) { mutableMapOf() }
            agencyCounts[agency] = agencyCounts.getOrDefault(agency, 0) + 1
        }
        
        val result = mutableMapOf<String, String>()
        for (entry in countryStats) {
            val country = entry.key
            val agencyCounts = entry.value

            val sortedAgencies = agencyCounts.entries.sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key }
            )

            result[country] = sortedAgencies.first().key
        }
        return result
    }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (allPlayers.isEmpty()) return Pair(0.0, 0.0)

        val costs = mutableListOf<Long>()
        for (p in allPlayers) {
            costs.add(p.transferCost)
        }
        costs.sort()

        val n = costs.size
        val median = if (n % 2 == 1) {
            costs[n / 2].toDouble()
        } else {
            (costs[n / 2 - 1] + costs[n / 2]) / 2.0
        }

        var totalGoals = 0
        for (p in allPlayers) {
            totalGoals += p.goals
        }
        
        if (totalGoals == 0) return Pair(0.0, 0.0)

        var lowGoals = 0
        var highGoals = 0
        for (p in allPlayers) {
            if (p.transferCost <= median) {
                lowGoals += p.goals
            } else {
                highGoals += p.goals
            }
        }

        return Pair(lowGoals.toDouble() / totalGoals, highGoals.toDouble() / totalGoals)
    }
}