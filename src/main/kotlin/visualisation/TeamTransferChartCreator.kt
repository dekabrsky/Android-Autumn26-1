package visualisation

import model.Player
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.CombinedDomainCategoryPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.data.Range
import org.jfree.data.category.DefaultCategoryDataset
import java.text.DecimalFormat

class TeamTransferChartCreator : ChartCreator {

    override fun create(players: List<Player>): JFreeChart {
        val teamCosts = getTeamCosts(players)
        val totalCost = teamCosts.sumOf { it.value }
        val topTeams = teamCosts.take(10)

        val costDataset = createTeamCostDataset(topTeams)
        val shareDataset = createCumulativeShareDataset(topTeams, totalCost)

        return createTopTeamsChart(costDataset, shareDataset)
    }

    private fun getTeamCosts(players: List<Player>) =
        players
            .groupBy { it.team.name }
            .mapValues { (_, teamPlayers) ->
                teamPlayers.sumOf { it.transferCost }
            }
            .entries
            .sortedByDescending { it.value }

    private fun createTeamCostDataset(
        teamCosts: List<Map.Entry<String, Long>>
    ): DefaultCategoryDataset {
        val dataset = DefaultCategoryDataset()

        teamCosts.forEach { (teamName, cost) ->
            dataset.addValue(cost, "Трансферная стоимость", teamName)
        }

        return dataset
    }

    private fun createCumulativeShareDataset(
        teamCosts: List<Map.Entry<String, Long>>,
        totalCost: Long
    ): DefaultCategoryDataset {
        val cumulativeShares = teamCosts
            .map { it.value }
            .runningFold(0L, Long::plus)
            .drop(1)
            .map { it.toDouble() / totalCost * 100 }

        val dataset = DefaultCategoryDataset()
        teamCosts.indices.forEach { index ->
            dataset.addValue(cumulativeShares[index], "Накопленная доля", teamCosts[index].key)
        }

        return dataset
    }

    private fun createTopTeamsChart(
        costDataset: DefaultCategoryDataset,
        shareDataset: DefaultCategoryDataset
    ): JFreeChart {
        val categoryAxis = CategoryAxis("Команда")
        categoryAxis.categoryLabelPositions =
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6)

        val costPlot = createCostPlot(costDataset)
        val sharePlot = createSharePlot(shareDataset)

        val combinedPlot = CombinedDomainCategoryPlot(categoryAxis)
        combinedPlot.add(costPlot, 3)
        combinedPlot.add(sharePlot, 2)

        return JFreeChart(
            "Топ-10 команд по суммарной трансферной стоимости", JFreeChart.DEFAULT_TITLE_FONT,
            combinedPlot, true)
    }

    private fun createCostPlot(
        dataset: DefaultCategoryDataset
    ): CategoryPlot {
        val axis = NumberAxis("Трансферная стоимость")
        axis.numberFormatOverride = DecimalFormat("#,##0")

        val renderer = BarRenderer()
        renderer.defaultItemLabelGenerator = StandardCategoryItemLabelGenerator(
            "{2}", DecimalFormat("#,##0"))
        renderer.defaultItemLabelsVisible = true

        return CategoryPlot(dataset, null, axis, renderer)
    }

    private fun createSharePlot(
        dataset: DefaultCategoryDataset
    ): CategoryPlot {
        val axis = NumberAxis("Накопленная доля, %")
        axis.range = Range(0.0, 100.0)
        axis.numberFormatOverride = DecimalFormat("0.0'%'")

        val renderer = LineAndShapeRenderer()
        renderer.defaultItemLabelGenerator = StandardCategoryItemLabelGenerator(
            "{2}", DecimalFormat("0.0"))
        renderer.defaultItemLabelsVisible = true

        return CategoryPlot(dataset, null, axis, renderer)
    }
}