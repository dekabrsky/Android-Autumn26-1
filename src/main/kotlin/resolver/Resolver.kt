package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    // 1. Игрки без агентства
    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    // 2. лучший защитник-бомбардир
    override fun getBestScorerDefender(): Pair<String, Int> {
        val best = players.asSequence()
            .filter { it.position == Position.DEFENDER }
            .sortedWith(
                compareByDescending<Player> { it.goals }
                    .thenBy { it.name }
            )
            .firstOrNull()
            ?: error("В выборке нет ни одного защитника")

        return best.name to best.goals
    }

    // 3. Русское название позиции самого дорогого немецкого игрока
    override fun getTheExpensiveGermanPlayerPosition(): String {
        val best = players.asSequence()
            .filter { it.country.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferValue }
            ?: error("В выборке нет ни одного немецкого игрока")

        return best.position.ruName
    }

    // 4. Команда с наибольшим средним числом красных карточек на игрока
    override fun getTheRudestTeam(): Team {
        return players.groupBy { it.team }
            .maxByOrNull { (_, list) -> list.map { it.redCards }.average() }
            ?.key
            ?: error("Выборка пуста")
    }

    // 5. ср трансферная стоимость по позициям, по убыванию средней
    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, list) -> list.map { it.transferValue }.average() }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }

    // 6. Топ 3 по «голы + 2 * передачи», при равенстве — по алфавиту имени
    override fun getMostValuablePlayers(): List<Player> =
        players.sortedWith(
            compareByDescending<Player> { it.goals + 2 * it.assists }
                .thenBy { it.name }
        ).take(3)

    // 7. Для каждой страны самое популярное агентство
    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.asSequence()
            .filter { it.agency != null }
            .groupBy { it.country }
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

    // 8. Доля голов игроков не дороже медианы и доля голов остальных
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0

        val costs = players.map { it.transferValue }.sorted()
        val median = if (costs.size % 2 == 1) {
            costs[costs.size / 2].toDouble()
        } else {
            (costs[costs.size / 2 - 1] + costs[costs.size / 2]) / 2.0
        }

        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0

        val goalsBelowOrEqual = players
            .filter { it.transferValue <= median }
            .sumOf { it.goals }

        val goalsAbove = totalGoals - goalsBelowOrEqual

        return goalsBelowOrEqual.toDouble() / totalGoals to
                goalsAbove.toDouble() / totalGoals
    }
}