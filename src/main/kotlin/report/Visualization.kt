package report

import model.Player
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y

data class CountryStat(
    val country: String,
    val count: Int,
    val percentage: Double
)

fun plotCountryProportions(players: List<Player>) {
    val totalPlayers = players.size

    val countryData = players
        .groupBy { it.nationality }
        .map { (country, teamPlayers) ->
            CountryStat(
                country = country,
                count = teamPlayers.size,
                percentage = teamPlayers.size.toDouble() / totalPlayers * 100
            )
        }
        .toList()

    val plotData = mapOf(
        Pair("nationality", countryData.map { it -> it.country }.toList()),
        Pair("percentage", countryData.map { it -> it.percentage }.toList())
    )

    val plot = plot(plotData) {
        layout {
            size = Pair(800, 600)
            title = "Доли игроков по странам"
        }

        bars {
            x("nationality")
            y("percentage")
        }

        x.axis.name = "Страна"
        y.axis.name = "Доля игроков, %"
    }

    plot.save("country_proportions.png", 1, 600)
    println("График долей игроков по странам сохранен в country_proportions.png")
}