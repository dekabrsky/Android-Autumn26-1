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

    // 2. Лучший бомбардир среди защитников
    override fun getBestScorerDefender(): Pair<String, Int> {
        val player = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?: error("Защитники не найдены")

        return Pair(player.name, player.goals)
    }

    // 3. Позиция самого дорогого немецкого игрока
    override fun getTheExpensiveGermanPlayerPosition(): String {
        val player = players
            .filter { it.country.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?: error("Немецкие игроки не найдены")

        return player.position.russianName
    }

    // 4. Команда с наибольшим средним числом красных карточек
    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) ->
                teamPlayers.map { it.redCards }.average()
            }
            ?.key
            ?: error("Команды не найдены")
    }

    // 5. Средняя стоимость игроков по позициям
    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) ->
                positionPlayers.map { it.transferCost }.average()
            }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }
    }

    // 6. Топ-3 по формуле: голы + 2 * передачи
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

    // 7. Самое популярное агентство по каждой стране
    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .groupBy { it.country }
            .mapValues { (_, countryPlayers) ->

                countryPlayers
                    .mapNotNull { it.agency }
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
                    .firstOrNull()
                    ?.key
                    ?: "Нет агентства"
            }
    }

    // 8. Доля голов относительно медианной стоимости
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val costs = players
            .map { it.transferCost }
            .sorted()

        if (costs.isEmpty()) {
            return Pair(0.0, 0.0)
        }

        val median = if (costs.size % 2 == 1) {
            costs[costs.size / 2].toDouble()
        } else {
            val left = costs[costs.size / 2 - 1]
            val right = costs[costs.size / 2]

            (left + right) / 2.0
        }

        val allGoals = players.sumOf { it.goals }.toDouble()

        if (allGoals == 0.0) {
            return Pair(0.0, 0.0)
        }

        val cheaperGoals = players
            .filter { it.transferCost.toDouble() <= median }
            .sumOf { it.goals }
            .toDouble()

        val expensiveGoals = players
            .filter { it.transferCost.toDouble() > median }
            .sumOf { it.goals }
            .toDouble()

        return Pair(
            cheaperGoals / allGoals,
            expensiveGoals / allGoals
        )
    }
}