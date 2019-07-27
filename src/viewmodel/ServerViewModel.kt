package fr.spoutnik87.viewmodel

data class ServerViewModel(
    val guildId: String,
    val queue: QueueViewModel,
    val currentlyPlaying: ContentViewModel?
)