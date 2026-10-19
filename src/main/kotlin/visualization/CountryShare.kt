package visualization

import model.Player

data class CountryShare(
    val country: String,
    val playersCount: Int,
    val share: Double,
)

fun calculateCountryShares(players: List<Player>): List<CountryShare> {
    val totalPlayers = players.size
    if (totalPlayers == 0) return emptyList()

    return players
        .groupingBy { it.nationality }
        .eachCount()
        .toList()
        .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
        .map { (country, count) ->
            CountryShare(
                country = country,
                playersCount = count,
                share = count.toDouble() / totalPlayers,
            )
        }
}
