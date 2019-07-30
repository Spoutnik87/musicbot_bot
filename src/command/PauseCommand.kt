package fr.spoutnik87.command

import fr.spoutnik87.bot.Server
import fr.spoutnik87.event.WebRequestEvent
import org.slf4j.LoggerFactory

class PauseCommand : WebCommand {

    private val logger = LoggerFactory.getLogger(PauseCommand::class.java)

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        server.pauseContent()
    }
}