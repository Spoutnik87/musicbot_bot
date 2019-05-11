package fr.spoutnik87.model

data class MusicbotRestServerModel(
    val id: String,
    val name: String,
    val ownerId: String,
    val linked: Boolean
) {
}