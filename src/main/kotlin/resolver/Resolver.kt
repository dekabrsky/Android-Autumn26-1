package resolver

import model.Player
import model.Position
import model.Team

class Resolver(
    private val players: List<Player>
) : IResolver {

    // 1. Количество игроков без агентства
    override fun getCountWithoutAgency(): Int {
        return players.count { it.agency == null }
    }

    // 2. Лучший по голам среди защитников и их кол-во
    override fun getBestScorerDefender(): Pair<String, Int> {
        return players
            .filter { it.position == Position.DEFENDER }
            .maxWithOrNull(
                compareBy<Player> { it.goals }
                    .thenBy { it.name }
            )
            ?.let { Pair(it.name, it.goals) }
            ?: Pair("", 0)
    }

    // 3. Позиция самого дорогого немецкого игрока
    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?.position
            ?.rusName
            ?: ""
    }

    // 4. Команда с наибольшим средним количеством красных карточек
    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) ->
                teamPlayers.map { it.redCards }.average()
            }
            ?.key
            ?: Team("", "")
    }

    // 5. Средняя трансферная стоимость по позициям
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

    // 6. Топ-3 самых полезных игроков
    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> {
                    it.goals + 2 * it.assists
                }.thenBy { it.name }
            )
            .take(3)
    }

    // 7. Самое популярное агентство для каждой страны
    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .groupBy { it.nationality }
            .mapValues { (_, countryPlayers) ->
                countryPlayers
                    .filter { it.agency != null }
                    .groupingBy { it.agency!! }
                    .eachCount()
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> { it.value }
                            .thenBy { it.key }
                    )
                    .firstOrNull()
                    ?.key
                    ?: ""
            }
    }

    // 8. Доля голов относительно медианной стоимости
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) {
            return Pair(0.0, 0.0)
        }

        val sortedCosts = players
            .map { it.transferCost }
            .sorted()

        val middle = sortedCosts.size / 2

        val median = if (sortedCosts.size % 2 == 0) {
            (sortedCosts[middle - 1] + sortedCosts[middle]) / 2.0
        } else {
            sortedCosts[middle].toDouble()
        }

        val totalGoals = players.sumOf { it.goals }

        if (totalGoals == 0) {
            return Pair(0.0, 0.0)
        }

        val goalsNotMoreThanMedian = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }

        val goalsMoreThanMedian = players
            .filter { it.transferCost > median }
            .sumOf { it.goals }

        return Pair(
            goalsNotMoreThanMedian.toDouble() / totalGoals,
            goalsMoreThanMedian.toDouble() / totalGoals
        )
    }
}