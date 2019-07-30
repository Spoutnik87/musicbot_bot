package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Configuration
import fr.spoutnik87.bot.Server
import org.slf4j.LoggerFactory

class LinkCommand(
    override val prefix: String
) : TextCommand {

    private val logger = LoggerFactory.getLogger(LinkCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        if (!messageEvent.message.content.isPresent
            || messageEvent.message.content.get().length <= Configuration.superPrefix.length + prefix.length + 1
        ) {
            return
        }
        val token = messageEvent.message.content.get().substring(Configuration.superPrefix.length + prefix.length + 1)
        if (!messageEvent.message.author.isPresent) {
            return
        }
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val userId = messageEvent.message.author.get().id.asString()
        val channel = messageEvent.message.channel.block() ?: return
        // messageEvent.message.delete().block()
        if (!server.linkable) {
            channel.createMessage("Ce serveur est déjà lié.").block()
            return
        }

        val result = server.linkServer(token, userId)
        if (result != null) {
            channel.createMessage("La liaison a été effectuée.").block()
        } else {
            channel.createMessage("La liaison n'a pas pu être effectuée.").block()
        }
    }
}