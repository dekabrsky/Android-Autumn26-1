package visualization

import model.Player
import model.Position
import model.Team
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max

object Charts {
    data class Point(val costMillions: Double, val goals: Double)

    fun movingAverage(players: List<Player>, window: Int = 5): List<Point> {
        require(window > 0)
        val sorted = players.sortedBy { it.transferCost }
        if (sorted.isEmpty()) return emptyList()
        return sorted.windowed(minOf(window, sorted.size)).map { group ->
            Point(group.map { it.transferCost / 1_000_000.0 }.average(), group.map { it.goals }.average())
        }
    }

    fun write(players: List<Player>, directory: File): List<File> {
        directory.mkdirs()
        val teamsFile = File(directory, "top-10-teams.png")
        val forwardsFile = File(directory, "forwards-price-goals.png")
        topTeams(Team.aggregate(players), teamsFile)
        forwards(players.filter { it.position == Position.FORWARD }, forwardsFile)
        return listOf(teamsFile, forwardsFile)
    }

    private fun canvas(title: String, subtitle: String, file: File, draw: (Graphics2D) -> Unit) {
        val image = BufferedImage(1400, 900, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.color = Color(248, 250, 253)
            g.fillRect(0, 0, 1400, 900)
            g.color = Color(24, 36, 58)
            g.font = Font("SansSerif", Font.BOLD, 30)
            g.drawString(title, 55, 60)
            g.font = Font("SansSerif", Font.PLAIN, 18)
            g.drawString(subtitle, 55, 95)
            draw(g)
            g.color = Color.DARK_GRAY
            g.font = Font("SansSerif", Font.PLAIN, 15)
            g.drawString("Источник: fakePlayersDirty.csv • только корректные строки", 55, 865)
        } finally { g.dispose() }
        check(ImageIO.write(image, "png", file))
    }

    private fun topTeams(teams: List<Team>, file: File) = canvas(
        "Топ-10 команд по трансферной стоимости", "Суммарная стоимость состава, млн (валюта в CSV не указана)", file
    ) { g ->
        val top = teams.sortedWith(compareByDescending<Team> { it.totalTransferCost }.thenBy { it.name }).take(10)
        val maximum = max(1.0, top.maxOfOrNull { it.totalTransferCost / 1_000_000.0 } ?: 0.0)
        top.forEachIndexed { index, team ->
            val y = 150 + index * 65
            g.color = Color(24, 36, 58)
            g.font = Font("SansSerif", Font.BOLD, 17)
            g.drawString(team.name, 55, y + 21)
            g.font = Font("SansSerif", Font.PLAIN, 14)
            g.drawString(team.city, 55, y + 42)
            val value = team.totalTransferCost / 1_000_000.0
            val width = (value / maximum * 760).toInt()
            g.color = Color(43, 111, 199)
            g.fillRoundRect(380, y, width, 40, 8, 8)
            g.color = Color(24, 36, 58)
            g.font = Font("SansSerif", Font.BOLD, 17)
            g.drawString("%.1f".format(value), 395 + width, y + 26)
        }
        if (top.isEmpty()) g.drawString("Нет данных", 55, 180)
    }

    private fun forwards(players: List<Player>, file: File) = canvas(
        "Нападающие: стоимость и голы", "Точка — игрок; цвет — жёлтые карточки; линия — скользящее среднее по 5 игрокам", file
    ) { g ->
        val left = 110; val bottom = 745; val width = 1120; val height = 580
        val maxCost = max(1.0, (players.maxOfOrNull { it.transferCost } ?: 0) / 1_000_000.0 * 1.05)
        val maxGoals = max(1.0, (players.maxOfOrNull { it.goals } ?: 0) * 1.1)
        fun x(cost: Double) = left + (cost / maxCost * width).toInt()
        fun y(goals: Double) = bottom - (goals / maxGoals * height).toInt()
        g.font = Font("SansSerif", Font.PLAIN, 16)
        for (i in 0..5) {
            val xx = left + width * i / 5; val yy = bottom - height * i / 5
            g.color = Color(216, 223, 233)
            g.drawLine(xx, bottom, xx, bottom - height)
            g.drawLine(left, yy, left + width, yy)
            g.color = Color.DARK_GRAY
            g.drawString("%.0f".format(maxCost * i / 5), xx - 12, bottom + 25)
            g.drawString("%.0f".format(maxGoals * i / 5), left - 40, yy + 5)
        }
        g.drawString("Голы", 55, 145)
        g.drawString("Трансферная стоимость, млн", 520, 803)
        fun cardColor(cards: Int) = when {
            cards == 0 -> Color(38, 139, 151)
            cards <= 3 -> Color(232, 172, 47)
            else -> Color(192, 68, 58)
        }
        players.forEach { player ->
            val xx = x(player.transferCost / 1_000_000.0); val yy = y(player.goals.toDouble())
            g.color = cardColor(player.yellowCards)
            g.fillOval(xx - 6, yy - 6, 12, 12)
        }
        g.color = Color(41, 44, 98)
        g.stroke = BasicStroke(3f)
        val trend = movingAverage(players)
        trend.zipWithNext().forEach { (a, b) -> g.drawLine(x(a.costMillions), y(a.goals), x(b.costMillions), y(b.goals)) }
        if (trend.size == 1) g.fillOval(x(trend[0].costMillions) - 4, y(trend[0].goals) - 4, 8, 8)
        listOf(0 to "0 карточек", 1 to "1–3 карточки", 4 to "4+ карточек").forEachIndexed { index, (cards, label) ->
            g.color = cardColor(cards)
            g.fillOval(760 + index * 180, 124, 12, 12)
            g.color = Color.DARK_GRAY
            g.drawString(label, 780 + index * 180, 136)
        }
        if (players.isEmpty()) g.drawString("Нет данных", 500, 400)
    }
}
