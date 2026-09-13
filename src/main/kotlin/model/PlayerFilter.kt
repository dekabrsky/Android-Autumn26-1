package model

sealed interface PlayerFilter {
    fun matches(player: Player): Boolean = when (this) {
        is ByPosition -> player.position == this.position
        is CheaperThan -> player.marketValue < this.value
        is And -> this.left.matches(player) && this.right.matches(player)
        is Or -> this.left.matches(player) || this.right.matches(player)
        is Not -> !this.filter.matches(player)
    }
}

data class ByPosition(val position: Position) : PlayerFilter
data class CheaperThan(val value: Long) : PlayerFilter
data class And(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
data class Or(val left: PlayerFilter, val right: PlayerFilter) : PlayerFilter
data class Not(val filter: PlayerFilter) : PlayerFilter