package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    // 1. Количество игроков без агентства.
    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    // 2. Лучший бомбардир среди защитников и его голы.
    override fun getBestScorerDefender(): Pair<String, Int> {
        val best = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?: throw NoSuchElementException("В выборке нет защитников")
        return best.name to best.goals
    }

    // 3. Русское название позиции самого дорогого немецкого игрока.
    override fun getTheExpensiveGermanPlayerPosition(): String =
        players
            .filter { it.nationality.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferCost }
            ?.position
            ?.russianName
            ?: throw NoSuchElementException("В выборке нет немецких игроков")

    // 4. Команда с наибольшим средним числом красных карточек на игрока.
    override fun getTheRudestTeam(): Team =
        players
            .groupBy { it.team }
            .mapValues { (_, teamPlayers) -> teamPlayers.map { it.redCards }.average() }
            .maxByOrNull { it.value }
            ?.key
            ?: throw NoSuchElementException("Выборка пуста")

    // 5. Средняя трансферная стоимость по позициям, по убыванию.
    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players
            .groupBy { it.position }
            .mapValues { (_, positionPlayers) -> positionPlayers.map { it.transferCost }.average() }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }

    // 6. Топ-3 по полезности «голы + 2 × передачи», при равенстве — по алфавиту имени.
    override fun getMostValuablePlayers(): List<Player> =
        players
            .sortedWith(compareByDescending<Player> { it.usefulness }.thenBy { it.name })
            .take(3)

    // 7. Самое популярное агентство для каждой страны, при равенстве — по алфавиту.
    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players
            .filter { it.agency != null }
            .groupBy { it.nationality }
            .mapValues { (_, countryPlayers) ->
                countryPlayers
                    .groupingBy { it.agency!! }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .first()
                    .key
            }

    // 8. Доли голов игроков не дороже медианы стоимости и дороже её.
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return 0.0 to 0.0
        val median = medianTransferCost()
        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0
        val cheapGoals = players.filter { it.transferCost <= median }.sumOf { it.goals }
        val cheapShare = cheapGoals.toDouble() / totalGoals
        return cheapShare to (1.0 - cheapShare)
    }

    /** Медиана трансферной стоимости (для чётного числа — среднее двух центральных). */
    fun medianTransferCost(): Double {
        val sorted = players.map { it.transferCost }.sorted()
        val n = sorted.size
        return if (n % 2 == 1) sorted[n / 2].toDouble()
        else (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
    }
}
