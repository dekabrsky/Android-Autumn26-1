package dsl

import model.Player

class ReportScope(val players: List<Player>, private val indent: String = "") {
    fun metric(name: String, valueProvider: ReportScope.() -> Any) {
        val value = valueProvider()
        println("$indent* $name: $value")
    }

    fun section(name: String, block: ReportScope.() -> Unit) {
        println("$indent- $name:")
        val nestedScope = ReportScope(players, "$indent  ")
        nestedScope.block()
    }
}

fun report(players: List<Player>, block: ReportScope.() -> Unit) {
    println("-".repeat(40))
    println("    Футбольный отчёт")
    println("-".repeat(40))

    val scope = ReportScope(players)
    scope.block()

    println("-".repeat(40))
}