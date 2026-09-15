package report

@DslMarker
annotation class ReportDsl

sealed interface ReportElement {

    fun render(
        builder: StringBuilder,
        indent: String
    )
}

data class MetricElement(
    val name: String,
    val value: Any?
) : ReportElement {

    override fun render(
        builder: StringBuilder,
        indent: String
    ) {
        builder
            .append(indent)
            .append(name)
            .append(": ")
            .append(value)
            .appendLine()
    }
}

data class SectionElement(
    val name: String,
    val elements: List<ReportElement>
) : ReportElement {

    override fun render(
        builder: StringBuilder,
        indent: String
    ) {
        builder
            .append(indent)
            .appendLine(name)

        elements.forEach { element ->
            element.render(
                builder = builder,
                indent = "$indent  "
            )
        }
    }
}

data class Report(
    val elements: List<ReportElement>
) {
    override fun toString(): String =
        buildString {
            elements.forEach { element ->
                element.render(
                    builder = this,
                    indent = ""
                )
            }
        }.trimEnd()
}

@ReportDsl
class ReportBuilder {

    private val elements = mutableListOf<ReportElement>()

    fun metric(
        name: String,
        value: () -> Any?
    ) {
        elements += MetricElement(
            name = name,
            value = value()
        )
    }

    fun section(
        name: String,
        block: ReportBuilder.() -> Unit
    ) {
        val sectionBuilder = ReportBuilder()
        sectionBuilder.block()

        elements += SectionElement(
            name = name,
            elements = sectionBuilder.build().elements
        )
    }

    fun build(): Report =
        Report(elements.toList())
}

fun buildReport(
    block: ReportBuilder.() -> Unit
): Report =
    ReportBuilder()
        .apply(block)
        .build()

fun report(
    block: ReportBuilder.() -> Unit
) {
    println(buildReport(block))
}