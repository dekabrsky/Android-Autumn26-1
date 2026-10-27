package visualization

import model.Player
import model.Position
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Color
import java.io.File

fun createVariant2Charts(players: List<Player>) {
    createTopTeamsChart(players)
    createForwardsChart(players)
}

private fun createTopTeamsChart(players: List<Player>) {

    val topTeams = players
        .groupBy { it.team }
        .map { (team, teamPlayers) ->

            val totalCost = teamPlayers.sumOf {
                it.transferCost
            }

            Pair(team.name, totalCost)
        }
        .sortedByDescending {
            it.second
        }
        .take(10)

    val dataset = DefaultCategoryDataset()

    topTeams.forEach { (teamName, totalCost) ->

        val costInMillions =
            totalCost / 1_000_000.0

        dataset.addValue(
            costInMillions,
            "Стоимость",
            teamName
        )
    }

    val chart = ChartFactory.createBarChart(
        "Топ-10 команд по трансферной стоимости",
        "Команда",
        "Стоимость, млн",
        dataset,
        PlotOrientation.VERTICAL,
        false,
        true,
        false
    )

    ChartUtils.saveChartAsPNG(
        File("variant2_top10_teams.png"),
        chart,
        1200,
        700
    )

    println("Создан график: variant2_top10_teams.png")
}

private fun createForwardsChart(players: List<Player>) {

    val forwards = players
        .filter {
            it.position == Position.FORWARD
        }
        .sortedBy {
            it.transferCost
        }

    val noCards = XYSeries("0 жёлтых карточек")
    val fewCards = XYSeries("1-2 жёлтые карточки")
    val manyCards = XYSeries("3+ жёлтых карточек")
    val trend = XYSeries("Скользящее среднее")

    forwards.forEach { player ->

        val costInMillions =
            player.transferCost / 1_000_000.0

        when {
            player.yellowCards == 0 -> {
                noCards.add(
                    costInMillions,
                    player.goals
                )
            }

            player.yellowCards <= 2 -> {
                fewCards.add(
                    costInMillions,
                    player.goals
                )
            }

            else -> {
                manyCards.add(
                    costInMillions,
                    player.goals
                )
            }
        }
    }

    // Среднее по 5 соседним игрокам
    forwards
        .windowed(size = 5, step = 1)
        .forEach { window ->

            val averageCost = window
                .map { it.transferCost }
                .average() / 1_000_000.0

            val averageGoals = window
                .map { it.goals }
                .average()

            trend.add(
                averageCost,
                averageGoals
            )
        }

    val dataset = XYSeriesCollection()

    dataset.addSeries(noCards)
    dataset.addSeries(fewCards)
    dataset.addSeries(manyCards)
    dataset.addSeries(trend)

    val chart = ChartFactory.createScatterPlot(
        "Стоимость и результативность нападающих",
        "Трансферная стоимость, млн",
        "Голы",
        dataset,
        PlotOrientation.VERTICAL,
        true,
        true,
        false
    )

    val renderer =
        chart.xyPlot.renderer as XYLineAndShapeRenderer

    // Цвет показывает карточки
    renderer.setSeriesPaint(0, Color.GREEN)
    renderer.setSeriesPaint(1, Color.ORANGE)
    renderer.setSeriesPaint(2, Color.RED)

    // Линия тренда
    renderer.setSeriesPaint(3, Color.BLUE)
    renderer.setSeriesLinesVisible(3, true)
    renderer.setSeriesShapesVisible(3, false)

    ChartUtils.saveChartAsPNG(
        File("variant2_forwards.png"),
        chart,
        1200,
        700
    )

    println("Создан график: variant2_forwards.png")
}