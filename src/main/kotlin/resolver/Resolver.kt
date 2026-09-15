package resolver

import model.Player
import model.Position
import model.Team
/*
1. Количество игроков, интересы которых не представляет агентство.
2. Автор наибольшего числа голов среди защитников и их количество.
3. Русское название позиции самого дорогого немецкого игрока.
4. Команда с наибольшим средним числом красных карточек на одного игрока.
5. Средняя трансферная стоимость по позициям — позиции отсортированы
по убыванию средней стоимости.
6. Топ-3 самых полезных игроков по формуле «голы + 2 × голевые передачи»
(при равенстве — раньше по алфавиту имени).
7. Для каждой страны — самое популярное среди её игроков агентство
(при равенстве — по алфавиту).
8. Посчитайте медианную трансферную стоимость и ответьте: какую долю всех
голов забили игроки не дороже медианы, а какую — дороже.
*/

class Resolver(
    private val players: List<Player>
) : IResolver {

    override fun getCountWithoutAgency(): Int {
        return players.count{it.agency == null}
    }

    override fun getBestScorerDefender(): Pair<String, Int> {
        return players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }
            ?.let { it.name to it.goals }
            ?: throw Exception("no DEFENDER")
    }

// Русское название позиции самого дорогого немецкого игрока.
    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players
            .filter { it.country == "Germany" }
            .maxByOrNull { it.transferCost }
            ?.let { it.position.russionPosition }
            ?: throw Exception("no players from Germany")
    }

// Команда с наибольшим средним числом красных карточек на одного игрока.
    override fun getTheRudestTeam(): Team {
    return players
        .groupBy { it.team }
        .maxBy { it.value.map { it.redCards }.average() }
        .key
    }

//Средняя трансферная стоимость по позициям — позиции отсортированы
//по убыванию средней стоимости.
    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
    return players
        .groupBy { it.position }
        .mapValues { (_, players) ->
            players.map { it.transferCost }.average()
        }
        .toList()
        .sortedByDescending { it.second }
        .toMap()
    }

// Топ-3 самых полезных игроков по формуле «голы + 2 × голевые передачи»
//(при равенстве — раньше по алфавиту имени).
    override fun getMostValuablePlayers(): List<Player> {
        return players
            .sortedWith(
                compareByDescending<Player> { it.goals + 2 * it.assists }
                    .thenBy { it.name }
            )
            .take(3)
    }
// Для каждой страны — самое популярное среди её игроков агентство
//(при равенстве — по алфавиту).
    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players
            .groupBy { it.country }
            .mapNotNull { (country, players) ->
                val agency = players
                    .mapNotNull { it.agency }
                    .groupingBy { it }
                    .eachCount()
                    .maxWithOrNull(
                        compareBy<Map.Entry<String, Int>> { it.value }
                            .thenByDescending { it.key }
                    )
                    ?.key

                agency?.let { country to it }
            }
            .toMap()
    }
// Посчитайте медианную трансферную стоимость и ответьте: какую долю всех
//голов забили игроки не дороже медианы, а какую — дороже.
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val costs = players
            .map { it.transferCost }
            .sorted()

        val median = if (costs.size % 2 == 1) {
            costs[costs.size / 2].toDouble()
        } else {
            (costs[costs.size / 2 - 1] + costs[costs.size / 2]) / 2.0
        }

        val totalGoals = players.sumOf { it.goals }

        val goalsNotMoreThanMedian = players
            .filter { it.transferCost <= median }
            .sumOf { it.goals }

        val goalsMoreThanMedian = players
            .filter { it.transferCost > median }
            .sumOf { it.goals }

        return Pair(
            goalsNotMoreThanMedian.toDouble() / totalGoals,
            goalsMoreThanMedian.toDouble() / totalGoals
        )
    }

}