package fr.spoutnik87.model

data class ContentViewModel(
    val id: String,
    val initiator: String,
    val duration: Long,
    val startTime: Long?,
    val position: Long?
)