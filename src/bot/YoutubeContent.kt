package fr.spoutnik87.bot

class YoutubeContent(
    uid: String,
    id: String,
    initiator: String,
    /**
     * Link to external content. May be null
     */
    val link: String,
    position: Long? = null,
    name: String? = null,
    duration: Long? = null
) : Content(uid, id, initiator, position, name, duration)
