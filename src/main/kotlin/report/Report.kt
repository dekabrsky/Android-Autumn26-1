package report

@DslMarker
annotation class ReportDsl

@ReportDsl
class ReportBuilder internal constructor(private val indent: String = "") {
    private val lines = mutableListOf<String>()

    fun metric(name: String, value: () -> Any?) {
        lines.add("$indent$name: ${value()}")
    }

    fun section(name: String, block: ReportBuilder.() -> Unit) {
        lines.add("$indent$name")
        lines.addAll(ReportBuilder("$indent  ").apply(block).lines)
    }

    internal fun render(): String = lines.joinToString("\n")
}

fun report(block: ReportBuilder.() -> Unit): String = ReportBuilder().apply(block).render()
