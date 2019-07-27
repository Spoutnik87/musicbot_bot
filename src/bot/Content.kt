package fr.spoutnik87.bot

class Content(
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
     * Link to external content. May be null
     */
    val link: String? = null,
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