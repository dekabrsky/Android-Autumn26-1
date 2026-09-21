package report

@DslMarker
annotation class ReportDsl

@ReportDsl
class ReportBuilder internal constructor(private val indentation: Int = 0) {
    private val lines = mutableListOf<String>()

    fun metric(name: String, value: () -> Any?) {
        lines += "${"  ".repeat(indentation)}$name: ${value()}"
    }

    fun section(name: String, block: ReportBuilder.() -> Unit) {
        lines += "${"  ".repeat(indentation)}$name"
        val child = ReportBuilder(indentation + 1).apply(block)
        lines += child.lines
    }

    internal fun build(): String = lines.joinToString(System.lineSeparator())
}

fun report(block: ReportBuilder.() -> Unit): String = ReportBuilder().apply(block).build()
