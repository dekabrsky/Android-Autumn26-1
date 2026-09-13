package report

class ReportBuilder internal constructor(private val indent: Int = 0) {
    private val lines = mutableListOf<String>()
    fun metric(name: String, value: () -> Any?) {
        lines += "${"  ".repeat(indent)}$name: ${value()}"
    }
    fun section(name: String, block: ReportBuilder.() -> Unit) {
        lines += "${"  ".repeat(indent)}$name:"
        val nested = ReportBuilder(indent + 1)
        nested.block()
        lines += nested.render()
    }

    internal fun render(): String = lines.joinToString(separator = "\n")
}

fun report(block: ReportBuilder.() -> Unit): String {
    val builder = ReportBuilder()
    builder.block()
    return builder.render()
}
