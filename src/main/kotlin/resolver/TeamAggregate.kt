package resolver

import model.Player
import model.Team
import java.util.Collections

/**
 * Снимок состава и производных показателей для задачи агрегирования команд.
 * Team остаётся описанием команды и не зависит от этого представления.
 */
class TeamAggregate(
    val team: Team,
    players: List<Player>
) {
    // Снимок защищает агрегат от последующих изменений переданной коллекции.
    val players: List<Player> = Collections.unmodifiableList(players.toList())

    val totalSquadValue: Long
        get() = players.sumOf { it.transferValue }

    val totalCards: Int
        get() = players.sumOf { it.yellowCards + it.redCards }

    val averageCards: Double
        get() = if (players.isEmpty()) 0.0 else totalCards.toDouble() / players.size

    val averageRedCards: Double
        get() = if (players.isEmpty()) 0.0
        else players.sumOf { it.redCards }.toDouble() / players.size
}
