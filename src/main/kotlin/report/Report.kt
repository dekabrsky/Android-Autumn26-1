package report

@DslMarker
annotation class ReportDsl

@ReportDsl
class ReportBuilder @PublishedApi internal constructor(
    private val indent: Int = 0,
    private val builder: StringBuilder = StringBuilder()
) {
    fun metric(name: String, value: Any?) {
        appendIndent()
        builder.append(name).append(": ").append(value).append('\n')
    }

    fun metric(name: String, value: () -> Any?) {
        metric(name, value())
    }

    fun section(name: String, block: ReportBuilder.() -> Unit) {
        appendIndent()
        builder.append(name).append(":\n")
        ReportBuilder(indent + 1, builder).block()
    }

    private fun appendIndent() {
        repeat(indent) { builder.append("  ") }
    }

    @PublishedApi
    internal fun render(): String = builder.toString().trimEnd()
}

inline fun report(block: ReportBuilder.() -> Unit): String {
    return ReportBuilder().apply(block).render()
}