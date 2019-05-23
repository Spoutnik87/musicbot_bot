package fr.spoutnik87.bot

import fr.spoutnik87.reader.UpdateContentPositionReader

class SetPositionCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        val reader = event.payload as UpdateContentPositionReader
        server.setContentPosition(reader.position)
    }
}