package resolver

import model.Player
import model.Position
import model.Team
import parser.CsvParser

class Resolver(
    private val players: List<Player> =
        CsvParser.parseResource("fakePlayersDirty.csv").players
) : IResolver {

    // 1. Игроки без агентства
    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    // 2. Лучший защитник-бомбардир
    override fun getBestScorerDefender(): Pair<String, Int> {
        val best = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?: return "" to 0
        return best.name to best.goals
    }

    // 3. Русское название позиции самого дорогого немца
    override fun getTheExpensiveGermanPlayerPosition(): String =
        players
            .filter { it.nationality == "Germany" }
            .maxByOrNull { it.transferCost }
            ?.position
            ?.russianName
            ?: ""

    // 4. Самая «грубая» команда по среднему числу красных карточек
    override fun getTheRudestTeam(): Team {
        return players
            .groupBy { it.team }
            .mapValues { (_, list) ->
                list.sumOf { it.redCards }.toDouble() / list.size
            }
            .maxByOrNull { it.value }
            ?.key
            ?: Team("", "")
    }

    // 5. Средняя стоимость по позициям, отсортировано по убыванию
    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players
            .groupBy { it.position }
            .map { (pos, list) ->
                pos to list.sumOf { it.transferCost }.toDouble() / list.size
            }
            .sortedByDescending { it.second }
            .toMap()   // LinkedHashMap сохраняет порядок

    // 6. Топ-3 полезных: goals + 2 * assists, при равенстве — по имени
    override fun getMostValuablePlayers(): List<Player> =
        players
            .sortedWith(
                compareByDescending<Player> { it.goals + 2 * it.assists }
                    .thenBy { it.name }
            )
            .take(3)

    // 7. Популярное агентство по странам (при равенстве — по алфавиту)
    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players
            .filter { it.agency != null }
            .groupBy { it.nationality }
            .mapValues { (_, list) ->
                list.groupingBy { it.agency!! }
                    .eachCount()
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> { it.value }
                            .thenBy { it.key }
                    )
                    .first()
                    .key
            }

    // 8. Доля голов «дешёвых» (<= медианы) и «дорогих» (> медианы)
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val costs = players.map { it.transferCost }.sorted()
        val n = costs.size
        val median: Double = if (n % 2 == 1) {
            costs[n / 2].toDouble()
        } else {
            (costs[n / 2 - 1] + costs[n / 2]) / 2.0
        }

        val totalGoals = players.sumOf { it.goals }.toDouble()
        if (totalGoals == 0.0) return 0.0 to 0.0

        val goalsCheap = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }
            .toDouble()
        val goalsExpensive = totalGoals - goalsCheap

        return (goalsCheap / totalGoals) to (goalsExpensive / totalGoals)
    }
}