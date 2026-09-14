package chart

import model.Player
import model.Team

data class CountryShare(val country: String, val playersCount: Int, val share: Double)

data class TeamCostShare(val team: Team, val totalCost: Long, val cumulativeShare: Double)

// Подготовка данных для графиков варианта 4 — отдельно от отрисовки, чтобы её можно было проверить тестами.
object ChartData {

    // Базовая: доля игроков каждой страны, по убыванию.
    fun countryShares(players: List<Player>): List<CountryShare> =
        players.groupingBy { it.nationality }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { (country, count) -> CountryShare(country, count, count.toDouble() / players.size) }

    // Продвинутая: топ команд по суммарной стоимости и накопленная доля от стоимости всех игроков.
    fun topTeamsByCost(players: List<Player>, limit: Int = 10): List<TeamCostShare> {
        val totalCost = players.sumOf { it.transferCost }
        val topTeams = players.groupBy { it.team }
            .mapValues { (_, teamPlayers) -> teamPlayers.sumOf { it.transferCost } }
            .entries
            .sortedByDescending { it.value }
            .take(limit)
        val cumulativeCosts = topTeams.runningFold(0L) { acc, entry -> acc + entry.value }.drop(1)
        return topTeams.zip(cumulativeCosts) { (team, cost), cumulative ->
            TeamCostShare(team, cost, cumulative.toDouble() / totalCost)
        }
    }
}
