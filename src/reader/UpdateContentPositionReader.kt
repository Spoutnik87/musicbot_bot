package fr.spoutnik87.reader

data class UpdateContentPositionReader(
    val id: String,
    val initiator: String,
    val position: Long
)