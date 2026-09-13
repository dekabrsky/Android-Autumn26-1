package dsl

class ReportBuilder() {

    private var indent = ""

    fun metric(name: String, action: () -> Any) {
        println("$indent$name: ${action()}")
    }

    fun section(name: String, block: ReportBuilder.() -> Unit) {
        println("\n-----$name-----")
        // Визуальный отступ для вложенных метрик
        indent = " "
        this.block()
    }
}

fun report(block: ReportBuilder.() -> Unit) {
    val builder = ReportBuilder()
    builder.block()
}