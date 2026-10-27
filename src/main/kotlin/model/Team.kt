package model

data class Team(
    val name: String,
    val city: String
) {
    val displayName: String
        get() = "$name ($city)"
}
