package report

data class Metric(val name: String, val value: Any?)

data class Section(val title: String, val metrics: MutableList<Metric> = mutableListOf())

class ReportBuilder {
    private val topLevelMetrics = mutableListOf<Metric>()
    private val sections = mutableListOf<Section>()

    fun metric(name: String, compute: () -> Any?) {
        topLevelMetrics += Metric(name, compute())
    }

    fun section(title: String, build: SectionBuilder.() -> Unit) {
        val section = Section(title)
        SectionBuilder(section).build()
        sections += section
    }

    fun render(): String = buildString {
        topLevelMetrics.forEach { appendLine("${it.name}: ${it.value}") }
        sections.forEach { section ->
            appendLine("== ${section.title} ==")
            section.metrics.forEach { appendLine("  ${it.name}: ${it.value}") }
        }
    }
}

class SectionBuilder(private val section: Section) {
    fun metric(name: String, compute: () -> Any?) {
        section.metrics += Metric(name, compute())
    }
}

fun report(build: ReportBuilder.() -> Unit): String {
    val builder = ReportBuilder()
    builder.build()
    return builder.render()
}