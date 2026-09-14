package resolver

import model.Player
import model.Position
import model.Team

interface IResolver {
    /** 1. Сколько игроков не представлены никаким агентством. */
    fun countPlayersWithoutAgency(): Int

    /** 2. Защитник с наибольшим числом голов и само число голов. */
    fun topScoringDefender(): Pair<Player, Int>?

    /** 3. Русское название позиции самого дорогого немецкого игрока. */
    fun mostExpensiveGermanPlayerPositionName(): String?

    /** 4. Команда с наибольшим средним числом красных карточек на игрока. */
    fun teamWithHighestAverageRedCards(): Team?

    /** 5. Средняя стоимость трансфера по позициям, по убыванию средней стоимости. */
    fun averageValueByPosition(): List<Pair<Position, Double>>

    /** 6. Топ-3 самых ценных игрока по формуле "голы + 2 × передачи". */
    fun top3ByImpactScore(): List<Player>

    /** 7. Для каждой страны — самое популярное агентство среди её игроков. */
    fun mostPopularAgencyByCountry(): Map<String, String?>

    /** 8. Медианная стоимость и доля голов у игроков дешевле/дороже медианы. */
    fun goalsShareAroundMedianValue(): MedianGoalsReport
}

data class MedianGoalsReport(
    val medianValue: Double,
    val goalsSharePercentBelowMedian: Double,
    val goalsSharePercentAboveMedian: Double
)