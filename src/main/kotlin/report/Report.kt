package report

class ReportSection(private val title: String?) {

    private val metrics = mutableListOf<Pair<String, Any?>>()
    private val subsections = mutableListOf<ReportSection>()

    fun metric(name: String, compute: () -> Any?) {
        metrics.add(name to compute())
    }

    fun section(title: String, block: ReportSection.() -> Unit) {
        subsections.add(ReportSection(title).apply(block))
    }

    fun render(indent: String = ""): String = buildString {
        title?.let { appendLine("$indent$it:") }
        val innerIndent = if (title != null) "$indent  " else indent
        metrics.forEach { (name, value) -> appendLine("$innerIndent$name: $value") }
        subsections.forEach { append(it.render(innerIndent)) }
    }
}

fun report(block: ReportSection.() -> Unit): String {
    val root = ReportSection(null)
    root.block()
    return root.render()
}
