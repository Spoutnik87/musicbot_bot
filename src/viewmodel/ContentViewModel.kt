package fr.spoutnik87.viewmodel

data class ContentViewModel(
    val uid: String,
    val id: String?,
    val initiator: String?,
    val startTime: Long?,
    val position: Long?,
    val paused: Boolean?,
    val name: String?,
    val duration: Long?
)