package visualization

import model.Player
import model.Team

class PositionDistribution(players: List<Player>) {

    val counts: Map<String, Int> = players
        .groupingBy { it.position.positionName }
        .eachCount()

    val totalPlayers: Int = players.size

    fun shareOf(positionName: String): Double {
        val count = counts[positionName] ?: return 0.0
        return count.toDouble() / totalPlayers
    }
}

class TopTeamsComposition(players: List<Player>, topN: Int = 5) {

    val teams: List<Team> = players
        .map { it.team }
        .distinct()
        .sortedByDescending { it.totalTransferCost }
        .take(topN)

    val teamPositionCounts: Map<String, Map<String, Int>> = teams.associate { team ->
        team.name to team.players
            .groupingBy { it.position.positionName }
            .eachCount()
    }

    val allPositions: List<String> = teamPositionCounts.values
        .flatMap { it.keys }
        .distinct()
        .sorted()
}