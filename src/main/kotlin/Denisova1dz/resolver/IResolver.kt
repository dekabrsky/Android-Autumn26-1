package Denisova1dz.resolver

import Denisova1dz.model.Player
import Denisova1dz.model.Position
import Denisova1dz.model.Team

interface IResolver {

    // 1. Выведите количество игроков, интересы которых не представляет агентство.
    fun getCountWithoutAgency(): Int

    // 2. Выведите автора наибольшего числа голов из числа защитников и их количество.
    fun getBestScorerDefender(): Pair<String, Int>

    // 3. Выведите русское название позиции самого дорогого немецкого игрока.
    fun getTheExpensiveGermanPlayerPosition(): String

    // 4. Выберите команду с наибольшим средним числом красных карточек на одного игрока.
    fun getTheRudestTeam(): Team

    // 5. Верните среднюю трансферную стоимость по позициям,
    //    отсортировав позиции по убыванию средней стоимости.
    fun getAverageTransferCostByPosition(): Map<Position, Double>

    // 6. Верните топ-3 самых полезных игроков по формуле «голы + 2 * голевые передачи».
    //    При равенстве полезности раньше в списке стоит игрок,
    //    чьё имя идёт раньше по алфавиту.
    fun getMostValuablePlayers(): List<Player>

    // 7. Для каждой страны верните самое популярное среди её игроков агентство.
    //    При равенстве выберите агентство, чьё название идёт раньше по алфавиту.
    fun getMostPopularAgencyByCountry(): Map<String, String>

    // 8. Разделите игроков медианой трансферной стоимости и верните пару долей:
    //    доля голов, забитых игроками не дороже медианы, и доля голов остальных.
    fun getGoalsShareByMedianCost(): Pair<Double, Double>
}
