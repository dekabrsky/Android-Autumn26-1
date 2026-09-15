package dsl

class ReportBuilder {
    private val output = StringBuilder()

    fun metric(name: String, valueProvider: () -> Any) {
        output.appendLine("$name: ${valueProvider()}")
    }

    fun section(name: String, init: SectionBuilder.() -> Unit) {
        output.appendLine("\n--- $name ---")
        val sectionBuilder = SectionBuilder(output)
        sectionBuilder.init()
    }

    fun build(): String = output.toString()
}

class SectionBuilder(private val output: StringBuilder) {
    fun metric(name: String, valueProvider: () -> Any) {
        output.appendLine("  $name: ${valueProvider()}")
    }
}

// Главная функция-входная точка DSL
fun report(init: ReportBuilder.() -> Unit): String {
    val builder = ReportBuilder()
    builder.init()
    return builder.build()
}