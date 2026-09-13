package report

class ReportBuilder {
    private val lines = mutableListOf<String>()
    private var indentLevel = 0

    fun metric(name: String, block: () -> Any?) {
        val value = block()
        lines += "${"  ".repeat(indentLevel)}$name: $value"
    }

    fun section(name: String, block: ReportBuilder.() -> Unit) {
        lines += "${"  ".repeat(indentLevel)}$name:"
        indentLevel++
        block()
        indentLevel--
    }

    fun build(): String = lines.joinToString(separator = "\n")
}

fun report(block: ReportBuilder.() -> Unit): String {
    val builder = ReportBuilder()
    builder.block()
    return builder.build()
}