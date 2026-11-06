package report

import model.Player

@DslMarker
annotation class ReportDsl

sealed interface ReportNode {
    data class Metric(val name: String, val value: String) : ReportNode
    data class Section(val title: String, val children: List<ReportNode>) : ReportNode
}

@ReportDsl
class SectionBuilder(val players: List<Player>) {
    private val nodes = mutableListOf<ReportNode>()

    fun metric(name: String, value: () -> Any?) {
        nodes += ReportNode.Metric(name, value().toString())
    }

    fun section(title: String, init: SectionBuilder.() -> Unit) {
        val child = SectionBuilder(players).apply(init)
        nodes += ReportNode.Section(title, child.build())
    }

    internal fun build(): List<ReportNode> = nodes.toList()
}

class Report(val nodes: List<ReportNode>) {
    fun render(): String = buildString { renderNodes(nodes, 0) }

    private fun StringBuilder.renderNodes(items: List<ReportNode>, depth: Int) {
        val pad = "  ".repeat(depth)
        for (node in items) {
            when (node) {
                is ReportNode.Metric -> appendLine("$pad${node.name}: ${node.value}")
                is ReportNode.Section -> {
                    appendLine("$pad== ${node.title} ==")
                    renderNodes(node.children, depth + 1)
                }
            }
        }
    }
}

fun report(players: List<Player>, init: SectionBuilder.() -> Unit): Report =
    Report(SectionBuilder(players).apply(init).build())
