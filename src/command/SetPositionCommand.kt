package fr.spoutnik87.command

import fr.spoutnik87.bot.Server
import fr.spoutnik87.event.WebRequestEvent
import fr.spoutnik87.reader.UpdateContentPositionReader
import org.slf4j.LoggerFactory

class SetPositionCommand : WebCommand {

    private val logger = LoggerFactory.getLogger(SetPositionCommand::class.java)

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val reader = event.payload as UpdateContentPositionReader
        server.setContentPosition(reader.position)
    }
}