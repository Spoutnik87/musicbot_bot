package fr.spoutnik87.model

data class MusicbotRestServerJoinModel(
    val userId: String,
    val guildId: String,
    val serverJoinToken: String
) {
}