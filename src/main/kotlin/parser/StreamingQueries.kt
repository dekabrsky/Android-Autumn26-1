package parser

import model.Player
import model.Position
import model.Team

fun findFirstEligiblePlayer(resourceName: String = "fakePlayersDirty.csv"): Player? {
    val stream = StreamingQueries::class.java.classLoader.getResourceAsStream(resourceName) ?: return null
    val teams = mutableMapOf<Pair<String, String>, Team>()

    return stream.bufferedReader().useLines { lines ->
        val iterator = lines.iterator()
        if (!iterator.hasNext()) return@useLines null
        val header = iterator.next().split(';').map(String::trim)
        val indexes = header.withIndex().associate { it.value to it.index }

        fun value(values: List<String>, name: String): String? = indexes[name]
            ?.let { values.getOrNull(it)?.trim() }

        while (iterator.hasNext()) {
            val values = iterator.next().split(';')
            val name = value(values, "Name").orEmpty()
            val teamName = value(values, "Team").orEmpty()
            val city = value(values, "City").orEmpty()
            val nationality = value(values, "Nationality").orEmpty()
            val position = Position.fromCsvTokenOrNull(value(values, "Position").orEmpty())
            val agency = value(values, "Agency")?.takeIf(String::isNotEmpty)
            val transferCost = value(values, "Transfer cost")?.toLongOrNull()
            val participations = value(values, "Participations")?.toIntOrNull()
            val goals = value(values, "Goals")?.toIntOrNull()
            val assists = value(values, "Assists")?.toIntOrNull()
            val yellowCards = value(values, "Yellow cards")?.toIntOrNull()
            val redCards = value(values, "Red cards")?.toIntOrNull()

            if (name.isEmpty() || teamName.isEmpty() || city.isEmpty() || nationality.isEmpty() ||
                position == null || transferCost == null || participations == null || goals == null ||
                assists == null || yellowCards == null || redCards == null) {
                continue
            }

            if (transferCost <= 50_000_000L || participations < 25 || yellowCards != 0 || redCards != 0) {
                continue
            }

            val team = teams.getOrPut(teamName to city) { Team(teamName, city) }
            return@useLines Player(
                name, team, position, nationality, agency, transferCost, participations,
                goals, assists, yellowCards, redCards
            )
        }
        null
    }
}

private object StreamingQueries
