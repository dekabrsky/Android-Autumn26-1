package visualization

import model.Player
import model.Team

data class TeamCostShare(
    val team: Team,
    val totalCost: Long,
    val cumulativeShare: Double,
)

fun calculateTopTeamCostShares(players: List<Player>, limit: Int = 10): List<TeamCostShare> {
    val totalCost = players.sumOf { it.transferCost }
    if (totalCost == 0L || limit <= 0) return emptyList()

    var cumulativeCost = 0L

    return players
        .groupBy { it.team }
        .mapValues { (_, teamPlayers) -> teamPlayers.sumOf { it.transferCost } }
        .toList()
        .sortedWith(compareByDescending<Pair<Team, Long>> { it.second }.thenBy { it.first.name }.thenBy { it.first.city })
        .take(limit)
        .map { (team, teamCost) ->
            cumulativeCost += teamCost
            TeamCostShare(
                team = team,
                totalCost = teamCost,
                cumulativeShare = cumulativeCost.toDouble() / totalCost,
            )
        }
}
