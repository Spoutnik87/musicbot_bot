package fr.spoutnik87.bot

import fr.spoutnik87.reader.PlayContentReader

class PlayContentCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        val reader = event.payload as PlayContentReader
        server.playContent(
            Content(
                reader.uid,
                reader.id,
                reader.initiator,
                reader.link,
                null,
                reader.name,
                reader.duration
            )
        )
    }
}