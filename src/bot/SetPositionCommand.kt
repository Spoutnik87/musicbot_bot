package fr.spoutnik87.bot

import fr.spoutnik87.reader.UpdateTrackPositionReader

class SetPositionCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        val reader = event.payload as UpdateTrackPositionReader
        server.setContentPosition(reader.position)
    }
}