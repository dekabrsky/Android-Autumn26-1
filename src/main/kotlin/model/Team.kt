package model

data class Team (val name: String, val city: String) {

    private val _players = mutableListOf<Player>()
    val players: List<Player> get() = _players

    fun addPlayer(player: Player) {
        _players.add(player)
    }
}