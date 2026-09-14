package resolver

import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {

    // 1. Выведите количество игроков, интересы которых не представляет агентство.
    override fun getCountWithoutAgency(): Int {
        return players.count { it.agency.isNullOrEmpty() }
    }

    // 2. Выведите автора наибольшего числа голов из числа защитников и их количество.
    override fun getBestScorerDefender(): Pair<String, Int> {
        val bestDefender = players
            .filter { it.position == Position.DEFENDER }
            .maxByOrNull { it.goals }

        return Pair(bestDefender?.name ?: "", bestDefender?.goals ?: 0)
    }

    // 3. Выведите русское название позиции самого дорогого немецкого игрока.
    override fun getTheExpensiveGermanPlayerPosition(): String {
        val expensiveGerman = players
            .filter { it.country.equals("Germany", ignoreCase = true) }
            .maxByOrNull { it.transferValue }

        return expensiveGerman?.position?.positionName ?: ""
    }

    // 4. Выберите команду с наибольшим средним числом красных карточек на одного игрока.
    override fun getTheRudestTeam(): Team {
        val teamStats = players.groupBy { it.team }

        val rudest = teamStats.maxByOrNull { (_, teamPlayers) ->
            teamPlayers.sumOf { it.redCards }.toDouble() / teamPlayers.size
        }

        return rudest?.key ?: Team("", "")
    }

    // 5. Верните среднюю трансферную стоимость по позициям,
    //    отсортировав позиции по убыванию средней стоимости.
    override fun getAverageTransferCostByPosition(): Map<Position, Double> {
        return players.groupBy { it.position }
            .mapValues { (_, teamPlayers) -> teamPlayers.map { it.transferValue }.average() }
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
                countryPlayers
                    .groupingBy { it.agency ?: "" }
                    .eachCount()
                    .maxWithOrNull(
                        compareByDescending<Map.Entry<String, Int>> { it.value }
                            .thenBy { it.key }  // при равенстве - по алфавиту (возрастание)
                    )?.key ?: ""
            }
    }

    // 8. Разделите игроков медианой трансферной стоимости и верните пару долей:
    //    доля голов, забитых игроками не дороже медианы, и доля голов остальных.
    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        // Ручной расчёт медианы для максимальной совместимости версий Kotlin
        val sortedCosts = players.map { it.transferValue }.sorted()
        val median = if (sortedCosts.isEmpty()) 0.0 else {
            val mid = sortedCosts.size / 2
            if (sortedCosts.size % 2 == 0) {
                (sortedCosts[mid - 1] + sortedCosts[mid]) / 2.0
            } else {
                sortedCosts[mid].toDouble()
            }
        }

        val goalsBelowOrEqual = players.filter { it.transferValue <= median }.sumOf { it.goals }
        val goalsAbove = players.filter { it.transferValue > median }.sumOf { it.goals }
        val totalGoals = goalsBelowOrEqual + goalsAbove

        val share1 = if (totalGoals == 0) 0.0 else goalsBelowOrEqual.toDouble() / totalGoals
        val share2 = if (totalGoals == 0) 0.0 else goalsAbove.toDouble() / totalGoals

        return Pair(share1, share2)
    }
}