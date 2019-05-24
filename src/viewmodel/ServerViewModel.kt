package fr.spoutnik87.viewmodel

import fr.spoutnik87.model.ContentViewModel
import fr.spoutnik87.model.QueueViewModel

data class ServerViewModel(
    val guildId: String,
    val queue: QueueViewModel,
    val currentlyPlaying: ContentViewModel?
)