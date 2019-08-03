package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Server
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory

class ResumeTextCommand(override val prefix: String) : TextCommand {

    private val logger = LoggerFactory.getLogger(ResumeTextCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val channel = messageEvent.message.channel.awaitFirst() ?: return
        server.resumeContent()
        channel.createMessage("Action effectuée").awaitFirst()
    }
}