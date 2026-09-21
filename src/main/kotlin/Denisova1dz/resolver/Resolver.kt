package Denisova1dz.resolver

import Denisova1dz.model.Player
import Denisova1dz.model.Team
import Denisova1dz.model.Position

class Resolver  (private val players: List<Player>) : IResolver {

    //игроки, чьи интересы не представляет агенство,
    // .isNullOrBlank() проверяет и на null, и на пустую строку, и на строку только из пробельных символов
    override fun getCountWithoutAgency(): Int = players.count { it.agency.isNullOrBlank() }

    //автор наибольшего числа голов из числа защитников и их количество
    override fun getBestScorerDefender(): Pair<String, Int> = players
        .filter { it.position == Position.DEFENDER } // фильтруем только защитников
        .maxBy { it.goals }                           // ищем защитника с максимальным числом голов
        .let { it.name to it.goals }                  // превращаем его в Pair(Имя, Голы)

    //русское название позиции самого дорогого немецкого игрока
    override fun getTheExpensiveGermanPlayerPosition(): String = players
        .filter { it.country == "Germany" }
        .maxByOrNull { it.transfer }
        ?.position
        ?.title ?: "Неизвестно"

    // команда с наибольшим средним числом красных карточек на одного игрока
    override fun getTheRudestTeam(): Team = players
        .groupBy { it.team } // группируем игроков по командам
        .maxByOrNull { it.value.map { player -> player.redCards }.average() }!!
        .key

    // средняя трансферная стоимость по позициям, отсортировывая позиции по убыванию средней стоимости
    override fun getAverageTransferCostByPosition(): Map<Position, Double> = players
        .groupBy { it.position }
        .mapValues { (_, positionPlayers) ->
            positionPlayers.map { it.transfer }.average()
        }
        .entries
        .sortedByDescending { it.value }
        .associate { it.key to it.value }

    // топ-3 самых полезных игроков по формуле «голы + 2 * голевые передачи»
    //    При равенстве полезности раньше в списке стоит игрок, чьё имя идёт раньше по алфавиту
    override fun getMostValuablePlayers(): List<Player> = players
        .sortedWith(compareBy(
            { -(it.goals + 2 * it.assists) },
            {it.name}
        ))
        .take(3)

    // для каждой страны вернуть самое популярное среди её игроков агентство,
    // при равенстве выбераем агентство, чьё название идёт раньше по алфавиту
    override fun getMostPopularAgencyByCountry(): Map<String, String> = players
        .filter { it.agency != null } // есть агентство
        .groupBy { it.country }      // группируем игроков по их странам
        .mapValues {
            it.value                 // it.value — список всех игроков текущей страны
                .groupBy { player -> player.agency!! } // группируем игроков этой страны по их агентствам
                .entries             // Агентство = Список его игроков
                .sortedWith(compareBy(
                    { agencyEntry -> -agencyEntry.value.size }, // минус сортирует по убыванию числа клиентов (чем больше, тем выше)
                    { agencyEntry -> agencyEntry.key }          // сортируем по алфавиту названия агентства
                ))
                .first().key         // 8. Берем первое (самое популярное) агентство из отсортированного списка
        }

    // разделите игроков медианой трансферной стоимости и верните пару долей:
    //    доля голов, забитых игроками не дороже медианы, и доля голов остальных.
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
    // сортировка стоимости всех игроков для поиска медианы
    val sortedTransfers = players.map { it.transfer }.sorted()
    val median = sortedTransfers[sortedTransfers.size / 2]
    // общее количество голов всех игроков
    val totalGoals = players.sumOf { it.goals }.toDouble()
    if (totalGoals == 0.0) return Pair(0.0, 0.0) // от деления на ноль
    // голы игроков не дороже медианы
    val goalsCheap = players
        .filter { it.transfer <= median }
        .sumOf { it.goals }
    // голы игроков дороже медианы
    val goalsExpensive = players
        .filter { it.transfer > median }
        .sumOf { it.goals }
    // доля дешевых , доля дорогих
    return Pair(goalsCheap / totalGoals, goalsExpensive / totalGoals)
}
}