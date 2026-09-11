package report

class ReportBuilder(
    private val content: StringBuilder = StringBuilder(),
    private val indent: Int = 0,
) {
    fun <T> metric(name: String, value: () -> T) {
        content.append("  ".repeat(indent)).append(name).append(": ").append(value()).append(System.lineSeparator())
    }

    fun section(title: String, body: ReportBuilder.() -> Unit) {
        content.append("  ".repeat(indent)).append(title).append(":").append(System.lineSeparator())
        ReportBuilder(content, indent + 1).body()
    }

    fun render(): String = content.toString()
}

fun report(build: ReportBuilder.() -> Unit) {
    val builder = ReportBuilder()
    builder.build()
    print(builder.render())
}