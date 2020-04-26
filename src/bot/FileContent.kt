package fr.spoutnik87.bot

class FileContent(
    uid: String,
    id: String,
    initiator: String,
    val fileName: String,
    position: Long? = null,
    name: String? = null,
    duration: Long? = null
) : Content(uid, id, initiator, position, name, duration)
