package fr.spoutnik87.viewmodel

data class ServerViewModel(
    // Unix time date
    val date: Long,
    val guildId: String,
    val queue: QueueViewModel,
    val currentlyPlaying: ContentViewModel?
)