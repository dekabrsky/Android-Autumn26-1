package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    // 1. Выведите количество игроков, интересы которых не представляет агентство.
    override fun getCountWithoutAgency(): Int =
        players.count { it.agency == null }

    // 2. Выведите автора наибольшего числа голов из числа защитников и их количество.
    override fun getBestScorerDefender(): Pair<String, Int> {
        val topDefender = players.filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }

        return if (topDefender != null) {
            Pair(topDefender.name, topDefender.goals)
        } else {
            Pair("", 0)
        }
    }

    // 3. Выведите русское название позиции самого дорогого немецкого игрока.
    override fun getTheExpensiveGermanPlayerPosition(): String {
        return players.filter { it.country.equals("Германия", ignoreCase = true) || it.country.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.marketValue }
            ?.position?.titleRu ?: ""
    }

    // 4. Выберите команду с наибольшим средним числом красных карточек на одного игрока.
    override fun getTheRudestTeam(): Team {
        return players.groupBy { it.team }
            .maxByOrNull { (_, teamPlayers) ->
                teamPlayers.sumOf { it.redCards }.toDouble() / teamPlayers.size
            }?.key ?: Team("", "")
    }

    // 5. Верните среднюю трансферную стоимость по позициям,
    //    отсортировав позиции по убыванию средней стоимости.
    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players.groupBy { it.position }
            .mapValues { (_, posPlayers) -> posPlayers.map { it.marketValue }.average() }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    // 6. Верните топ-3 самых полезных игроков по формуле «голы + 2 * голевые передачи».
    //    При равенстве полезности раньше в списке стоит игрок, чьё имя идёт раньше по алфавиту.
    override fun getMostValuablePlayers(): List<Player> {
        return players.sortedWith(
            compareByDescending<Player> { it.goals + 2 * it.assists }
                .thenBy { it.name }
        ).take(3)
    }

    // 7. Для каждой страны верните самое популярное среди её игроков агентство.
    //    При равенстве выберите агентство, чьё название идёт раньше по алфавиту.
    override fun getMostPopularAgencyByCountry(): Map<String, String> {
        return players.groupBy { it.country }
            .mapValues { (_, countryPlayers) ->
                countryPlayers.mapNotNull { it.agency }
                    .groupBy { it }
                    .mapValues { it.value.size }
                    .entries
                    .sortedWith(
                        compareByDescending<Map.Entry<String, Int>> { it.value }
                            .thenBy { it.key }
                    )
                    .firstOrNull()?.key ?: ""
            }
            .filterValues { it.isNotEmpty() }
    }

    // 8. Разделите игроков медианой трансферной стоимости и верните пару долей:
    //    доля голов, забитых игроками не дороже медианы, и доля голов остальных.
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        if (players.isEmpty()) return Pair(0.0, 0.0)

        val sortedValues = players.map { it.marketValue }.sorted()
        val median = if (sortedValues.size % 2 == 1) {
            sortedValues[sortedValues.size / 2].toDouble()
        } else {
            (sortedValues[sortedValues.size / 2 - 1] + sortedValues[sortedValues.size / 2]) / 2.0
        }

        val totalGoals = players.sumOf { it.goals }.toDouble()
        if (totalGoals == 0.0) return Pair(0.0, 0.0)

        val goalsLessOrEqualMedian = players.filter { it.marketValue <= median }.sumOf { it.goals }
        val goalsGreaterMedian = players.filter { it.marketValue > median }.sumOf { it.goals }

        return Pair(goalsLessOrEqualMedian / totalGoals, goalsGreaterMedian / totalGoals)
    }
}