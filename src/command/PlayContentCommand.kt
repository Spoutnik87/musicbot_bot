package fr.spoutnik87.bot

import fr.spoutnik87.reader.PlayContentReader

class PlayContentCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        val reader = event.payload as PlayContentReader
        if (server.bot.canJoin(reader.initiator)) {
            server.playContent(Content(reader.uid, reader.id, reader.initiator))
        }
    }
}