package resolver

import model.Player
import model.Position
import model.Team

/**
 * Ответы на запросы задания 2 по готовой выборке игроков @param players игроки, прошедшие маппинг в [parser.CsvParser]
 */
class Resolver(private val players: List<Player>) : IResolver {

    private companion object {
        const val GERMANY = "Germany"
        const val TOP_PLAYERS = 3
    }

    /** Количество игроков, интересы которых не представляет агентство. */
    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    /**
     * Защитник с наибольшим числом голов и это число при равенстве голов выбирается игрок, чьё имя идёт раньше по алфавиту
     */
    override fun getBestScorerDefender(): Pair<String, Int> {
        val best = players.asSequence()
            .filter { it.position == Position.DEFENDER }
            .sortedWith(compareByDescending<Player> { it.goals }.thenBy { it.name })
            .firstOrNull()
            ?: error("В выборке нет защитников")
        return best.name to best.goals
    }

    /** Русское название позиции самого дорогого немецкого игрока */
    override fun getTheExpensiveGermanPlayerPosition(): String {
        val german = players.asSequence()
            .filter { it.country.equals(GERMANY, ignoreCase = true) }
            .sortedWith(compareByDescending<Player> { it.transferCost }.thenBy { it.name })
            .firstOrNull()
            ?: error("В выборке нет игроков из страны '$GERMANY'")
        return german.position.russianName
    }

    /** Команда с наибольшим средним числом красных карточек на одного игрока */
    override fun getTheRudestTeam(): Team {
        require(players.isNotEmpty()) { "Выборка игроков пуста" }
        return players.groupBy { it.team }
            .mapValues { (_, squad) -> squad.sumOf { it.redCards }.toDouble() / squad.size }
            .entries
            .sortedWith(compareByDescending<Map.Entry<Team, Double>> { it.value }.thenBy { it.key.title })
            .first()
            .key
    }

    /** Средняя трансферная стоимость по позициям, позиции по убыванию стоимости */
    override fun getAverageTransferCostByPosition(): Map<Position, Double> =
        players.groupBy { it.position }
            .mapValues { (_, group) -> group.sumOf { it.transferCost }.toDouble() / group.size }
            .toList()
            .sortedWith(compareByDescending<Pair<Position, Double>> { it.second }.thenBy { it.first.name })
            .toMap()

    /**
     * Топ-3 самых полезных игроков по формуле «голы + 2 × голевые передачи».
     * При равной полезности раньше идёт игрок с именем раньше по алфавиту.
     */
    override fun getMostValuablePlayers(): List<Player> =
        players.sortedWith(compareByDescending<Player> { it.usefulness }.thenBy { it.name })
            .take(TOP_PLAYERS)

    /**
     * Для каждой страны — самое популярное среди её игроков агентство.
     * При равенстве выбирается агентство, чьё название идёт раньше по алфавиту.
     * Страны, где ни у одного игрока нет агентства, в результат не попадают.
     */
    override fun getMostPopularAgencyByCountry(): Map<String, String> =
        players.asSequence()
            .filter { it.agency != null }
            .groupBy({ it.country }, { it.agency!! })
            .mapValues { (_, agencies) ->
                agencies.groupingBy { it }.eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                    .first()
                    .key
            }
            .toSortedMap()

    /**
     * Доли голов относительно медианной трансферной стоимости:
     * первая — доля голов игроков не дороже медианы, вторая — доля остальных
     * Сумма долей равна 1.0 (или 0.0 и 0.0, если голов не было вовсе)
     */
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        require(players.isNotEmpty()) { "Выборка игроков пуста" }

        val median = medianTransferCost()
        val totalGoals = players.sumOf { it.goals }
        if (totalGoals == 0) return 0.0 to 0.0

        val cheapGoals = players.filter { it.transferCost <= median }.sumOf { it.goals }
        val cheapShare = cheapGoals.toDouble() / totalGoals
        return cheapShare to (1.0 - cheapShare)
    }

    /** Медианная трансферная стоимость выборки. */
    fun medianTransferCost(): Double {
        require(players.isNotEmpty()) { "Выборка игроков пуста" }
        val sorted = players.map { it.transferCost }.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 1) {
            sorted[middle].toDouble()
        } else {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        }
    }
}
