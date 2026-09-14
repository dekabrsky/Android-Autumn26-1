package report

import java.util.Locale

/**
 * Маркер DSL: не даёт случайно вызвать `metric` внешнего скоупа
 * изнутри вложенной секции.
 */
@DslMarker
annotation class ReportDsl

/** Узел отчёта: либо метрика, либо секция с вложенными узлами. */
sealed interface ReportNode {
    val title: String
}

/**
 * Одна строка отчёта. Значение вычисляется лениво, в момент рендеринга,
 * поэтому описание отчёта можно собрать заранее.
 */
class Metric(
    override val title: String,
    val value: () -> Any?,
) : ReportNode

/** Группа строк с заголовком. */
class Section(
    override val title: String,
    val nodes: List<ReportNode>,
) : ReportNode

/**
 * Скоуп DSL: то, что доступно внутри блоков `report { }` и `section { }`.
 *
 * Работает на лямбдах с приёмником: блок получает [SectionScope] как `this`,
 * поэтому внутри можно писать `metric(...)` без префиксов.
 */
@ReportDsl
class SectionScope internal constructor() {

    private val nodes = mutableListOf<ReportNode>()

    /** Метрика: подпись и лямбда, которая вернёт значение. */
    fun metric(title: String, value: () -> Any?) {
        nodes += Metric(title, value)
    }

    /** Метрика с уже готовым значением. */
    fun value(title: String, value: Any?) {
        nodes += Metric(title) { value }
    }

    /** Вложенная секция. */
    fun section(title: String, block: SectionScope.() -> Unit) {
        nodes += Section(title, SectionScope().apply(block).build())
    }

    internal fun build(): List<ReportNode> = nodes.toList()
}

/** Готовый отчёт: заголовок и дерево узлов. */
class Report internal constructor(
    private val title: String,
    private val nodes: List<ReportNode>,
) {

    /** Собирает отчёт в текст. */
    fun render(): String = buildString {
        appendLine(title)
        appendLine("=".repeat(title.length))
        nodes.forEach { appendNode(it, level = 0) }
    }

    /** Печатает отчёт в консоль. */
    fun print() = kotlin.io.print(render())

    private fun StringBuilder.appendNode(node: ReportNode, level: Int) {
        val indent = "  ".repeat(level)
        when (node) {
            is Metric -> appendLine("$indent${node.title}: ${format(node.value())}")
            is Section -> {
                appendLine()
                appendLine("$indent[${node.title}]")
                node.nodes.forEach { appendNode(it, level + 1) }
            }
        }
    }

    private fun format(value: Any?): String = when (value) {
        null -> "нет данных"
        is Double -> if (value.isFinite()) String.format(Locale.US, "%,.2f", value) else value.toString()
        is Float -> format(value.toDouble())
        is Long -> String.format(Locale.US, "%,d", value)
        is Int -> String.format(Locale.US, "%,d", value)
        is Iterable<*> -> value.joinToString(separator = ", ") { format(it) }
        is Map<*, *> -> value.entries.joinToString(separator = ", ") { "${format(it.key)} = ${format(it.value)}" }
        is Pair<*, *> -> "${format(value.first)} / ${format(value.second)}"
        else -> value.toString()
    }
}

/**
 * Точка входа DSL. Отчёт описывается декларативно:
 *
 * ```
 * report("Чемпионат") {
 *     metric("Без агентства") { players.count { it.agency == null } }
 *     section("Атаки") {
 *         metric("Всего голов") { players.sumOf { it.goals } }
 *     }
 * }
 * ```
 */
fun report(title: String, block: SectionScope.() -> Unit): Report =
    Report(title, SectionScope().apply(block).build())
