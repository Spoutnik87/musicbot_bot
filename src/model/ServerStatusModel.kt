package fr.spoutnik87.model

import java.util.*

data class ContentStatusModel(
    val uid: String,
    val id: String?,
    val initiator: String?,
    val startTime: Long?,
    val position: Long?,
    val paused: Boolean?,
    val name: String?,
    val duration: Long?
)

data class ServerStatusModel(val date: Date, val playing: ContentStatusModel, val trackList: List<ContentStatusModel>)