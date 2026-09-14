package model

/**
 * Футболист и его статистика за чемпионат.
 * Единственное nullable-поле — [agency]. Значение null означает,
 * что интересы игрока никто не представляет; это часть предметной области, а не ошибка данных
 */
data class Player(
    val name: String,
    val team: Team,
    val position: Position,
    val country: String,
    val agency: String?,
    val transferCost: Long,
    val participations: Int,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
) {
    val usefulness: Int get() = goals + 2 * assists

    val hasAgency: Boolean get() = agency != null

    val totalCards: Int get() = yellowCards + redCards
}
