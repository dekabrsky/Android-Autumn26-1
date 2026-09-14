package model

/**
 * Команда чемпионата. Идентифицируется парой (название + город)
 */
data class Team(
    val name: String,
    val city: String,
) {
    val title: String get() = "$name ($city)"
}
