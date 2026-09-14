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
            .sortedWith(
                compareByDescending<Player> { it.goals }
                    .thenBy { it.name }
            )
            .firstOrNull()

        return if (player == null) {
            "" to 0
        } else {
            player.name to player.goals
        }
    }

    // 3. Позиция самого дорогого немецкого игрока
    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .filter {
                it.country.equals(
                    "Germany",
                    ignoreCase = true
                )
            }
            .sortedWith(
                compareByDescending<Player> { it.transferValue }
                    .thenBy { it.name }
            )
            .firstOrNull()
            ?.position
            ?.russianName
            .orEmpty()
    }

    // 4. Команда с наибольшим средним числом красных карточек
    override fun getTheRudestTeam(): Team {
        require(players.isNotEmpty()) {
            "Список игроков пуст"
        }

        return players
            .groupBy { it.team }
            .entries
            .sortedWith(
                compareByDescending<Map.Entry<Team, List<Player>>> {
                        entry ->
                    entry.value
                        .map { player -> player.redCards }
                        .average()
                }.thenBy { entry ->
                    entry.key.name
                }
            )
            .first()
            .key
    }

    // 5. Средняя трансферная стоимость по позициям
    override fun getAverageTransferCostByPosition():
            Map<Position, Double> {

        val averages = players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) ->
                positionPlayers
                    .map { it.transferValue }
                    .average()
            }

        val result = linkedMapOf<Position, Double>()

        averages.entries
            .sortedWith(
                compareByDescending<Map.Entry<Position, Double>> {
                    it.value
                }.thenBy {
                    it.key.name
                }
            )
            .forEach {
                result[it.key] = it.value
            }

        return result
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

    // 7. Самое популярное агентство для каждой страны
    override fun getMostPopularAgencyByCountry():
            Map<String, String> {

        val result = linkedMapOf<String, String>()

        players
            .groupBy { it.country }
            .toSortedMap()
            .forEach { (country, countryPlayers) ->
                val agency = countryPlayers
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

                if (agency != null) {
                    result[country] = agency
                }
            }

        return result
    }

    // 8. Доли голов относительно медианной стоимости
    override fun getGoalsShareByMedianCost():
            Pair<Double, Double> {

        if (players.isEmpty()) {
            return 0.0 to 0.0
        }

        val costs = players
            .map { it.transferValue.toDouble() }
            .sorted()

        val middle = costs.size / 2

        val median = if (costs.size % 2 == 1) {
            costs[middle]
        } else {
            (costs[middle - 1] + costs[middle]) / 2.0
        }

        val totalGoals = players.sumOf { it.goals }

        if (totalGoals == 0) {
            return 0.0 to 0.0
        }

        val cheaperGoals = players
            .filter { it.transferValue <= median }
            .sumOf { it.goals }

        val expensiveGoals = totalGoals - cheaperGoals

        val cheaperShare =
            cheaperGoals.toDouble() / totalGoals

        val expensiveShare =
            expensiveGoals.toDouble() / totalGoals

        return cheaperShare to expensiveShare
    }
}