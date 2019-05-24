package fr.spoutnik87.model

data class ContentViewModel(
    val uid: String,
    val id: String,
    val initiator: String,
    val startTime: Long?,
    val position: Long?,
    val paused: Boolean?
)