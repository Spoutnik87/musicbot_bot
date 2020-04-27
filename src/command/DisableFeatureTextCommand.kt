package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Server
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory

class DisableFeatureTextCommand(override val prefix: String) : TextCommand {

    private val logger = LoggerFactory.getLogger(DisableFeatureTextCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val channel = messageEvent.message.channel.awaitFirst() ?: return
        val options = messageEvent.message.content.get().split(" ").filter { it != "" }
        if (options.size < 2) return
        server.setFeature(options[1], false)
        channel.createMessage("Action effectuée").awaitFirst()
    }
}
