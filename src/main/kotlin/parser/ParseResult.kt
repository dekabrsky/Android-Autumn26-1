package parser

import model.Player

data class ParseResult(val players: List<Player>, val skipped: Int)
