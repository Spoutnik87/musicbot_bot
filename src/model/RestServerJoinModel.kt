package fr.spoutnik87.model

data class RestServerJoinModel(
    val userId: String,
    val guildId: String,
    val serverJoinToken: String
) {
}