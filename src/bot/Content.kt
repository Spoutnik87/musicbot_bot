package fr.spoutnik87.bot

class Content(
    /**
     * Used to identify this content in queue.
     */
    val uid: String,
    /**
     * Content UUID
     */
    val id: String,
    /**
     * The user who added this Content
     * User UUID
     */
    val initiator: String
)