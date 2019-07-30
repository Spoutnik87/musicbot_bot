package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Server
import org.slf4j.LoggerFactory

class PauseTextCommand(override val prefix: String) : TextCommand {

    private val logger = LoggerFactory.getLogger(PauseTextCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val channel = messageEvent.message.channel.block() ?: return
        server.pauseContent()
        channel.createMessage("Action effectuée").block()
    }
}