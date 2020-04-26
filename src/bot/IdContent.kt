package fr.spoutnik87.bot

class IdContent(
    uid: String,
    id: String?,
    initiator: String,
    position: Long? = null,
    name: String? = null,
    duration: Long? = null
) : Content(uid, id, initiator, position, name, duration)
