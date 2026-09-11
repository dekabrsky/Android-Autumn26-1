package resolver

import model.Player
import model.Position
import model.Team

interface IResolver {

    // Количество игроков, интересы которых не представляет агентство
    fun getCountWithoutAgency(): Int

    // Автор наибольшего числа голов из числа защитников и их количество
    fun getBestScorerDefender(): Pair<String, Int>

    // Название позиции самого дорогого немецкого игрока.
    fun getTheExpensiveGermanPlayerPosition(): String

    // Команда с наибольшим средним числом красных карточек на одного игрока
    fun getTheRudestTeam(): Team

    // Средняя трансферная стоимость по позициям, позиции отсортированы по убыванию средней стоимости
    fun getAverageTransferCostByPosition(): Map<Position, Double>

    // Топ-3 самых полезных игроков по формуле «голы + 2 * голевые передачи»
    fun getMostValuablePlayers(): List<Player>

    // Для каждой страны самое популярное среди её игроков агентство
    fun getMostPopularAgencyByCountry(): Map<String, String>

    // 8. Доля голов, забитых игроками не дороже медианной трансферной стоимости, и доля голов остальных игроков
    fun getGoalsShareByMedianCost(): Pair<Double, Double>
}
