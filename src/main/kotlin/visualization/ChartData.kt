package visualization

import model.Player
import model.Team

data class TeamCostShare(
    val team: Team,
    val totalCost: Long,
    val cumulativeShare: Double
)

fun calculateTopTeamCostShares(
    players: List<Player>,
    limit: Int = 10
): List<TeamCostShare> {
    require(limit >= 0) {
        "Количество команд не может быть отрицательным"
    }

    val teams = Team.aggregate(players)
        .sortedWith(
            compareByDescending<Team> {
                it.totalTransferCost
            }.thenBy {
                it.name
            }
        )

    val totalCost = teams.sumOf {
        it.totalTransferCost
    }

    val topTeams = teams.take(limit)

    val cumulativeCosts = topTeams
        .map { it.totalTransferCost }
        .runningFold(0L) { accumulated, cost ->
            accumulated + cost
        }
        .drop(1)

    return topTeams.mapIndexed { index, team ->
        TeamCostShare(
            team = team,
            totalCost = team.totalTransferCost,
            cumulativeShare = if (totalCost == 0L) {
                0.0
            } else {
                cumulativeCosts[index].toDouble() /
                        totalCost
            }
        )
    }
}