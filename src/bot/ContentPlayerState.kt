package fr.spoutnik87.bot

data class ContentPlayerState(
    /**
     * Playing content.
     */
    val content: Content?,
    val playing: Boolean,
    val paused: Boolean,
    val startTime: Long?,
    val position: Long?
)