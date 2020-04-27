package fr.spoutnik87.bot

abstract class Content(
    /**
     * Used to identify this content in queue.
     */
    val uid: String,
    /**
     * Content UUID
     * Can be null when a content is played from a command.
     */
    val id: String?,
    /**
     * The user who added this Content
     * User Discord UUID
     */
    val initiator: String,

    /**
     * Preferred start position
     */
    val position: Long? = null,
    /**
     * Content name
     */
    val name: String? = null,
    /**
     * Content duration
     */
    val duration: Long? = null
)
