package report

fun report(block: ReportBuilder.() -> Unit): String =
    ReportBuilder().apply(block).build()

class ReportBuilder internal constructor(
    private val indent: String = "",
) {
    internal val lines = mutableListOf<String>()

    fun metric(name: String, valueProvider: () -> Any?) {
        lines += "$indent$name: ${valueProvider()}"
    }

    fun section(name: String, block: ReportBuilder.() -> Unit) {
        lines += "$indent$name"
        lines += ReportBuilder(indent + INDENT_STEP).apply(block).lines
    }

    fun build(): String = lines.joinToString(System.lineSeparator())

    private companion object {
        const val INDENT_STEP = "  "
    }
}
