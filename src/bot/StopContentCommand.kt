package fr.spoutnik87.bot

import fr.spoutnik87.reader.StopTrackReader

class StopContentCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        val reader = event.payload as StopTrackReader
        if (reader.uid == server.playingContent?.uid) {
            server.stopPlayingContent()
            server.playNextContent()
        } else {
            server.queue.removeContent(reader.uid)
        }
    }
}