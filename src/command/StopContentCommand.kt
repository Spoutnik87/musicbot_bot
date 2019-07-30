package fr.spoutnik87.command

import fr.spoutnik87.bot.Server
import fr.spoutnik87.event.WebRequestEvent
import fr.spoutnik87.reader.StopContentReader
import org.slf4j.LoggerFactory

class StopContentCommand : WebCommand {

    private val logger = LoggerFactory.getLogger(StopContentCommand::class.java)

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val reader = event.payload as StopContentReader
        if (reader.uid == server.player.getPlayingContent()?.uid) {
            server.stopPlayingContent()
            server.playNextContent()
        } else {
            server.queue.removeContent(reader.uid)
        }
    }
}