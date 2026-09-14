package report

class ReportScope internal constructor(private val output: MutableList<String>) {
    fun metric(name: String, value: () -> Any?) {
        output += "$name: ${value()}"
    }

    fun section(name: String, content: ReportScope.() -> Unit) {
        output += name
        ReportScope(output).content()
    }
}

fun report(content: ReportScope.() -> Unit): String {
    val output = mutableListOf<String>()
    ReportScope(output).content()
    return output.joinToString(System.lineSeparator())
}
