package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Configuration
import fr.spoutnik87.bot.Server
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory

class LinkCommand(
    override val prefix: String
) : TextCommand {

    private val logger = LoggerFactory.getLogger(LinkCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        if (!Configuration.restApi) return
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        if (!messageEvent.message.content.isPresent) {
            return
        }
        val options = messageEvent.message.content.get().split(" ").filter { it != "" }
        if (options.size < 2) return;
        val token = options[1]
        if (!messageEvent.message.author.isPresent) {
            return
        }
        val userId = messageEvent.message.author.get().id.asString()
        val channel = messageEvent.message.channel.awaitFirst() ?: return
        // messageEvent.message.delete().block()
        if (!server.linkable) {
            channel.createMessage("Ce serveur est déjà lié.").awaitFirst()
            return
        }

        val result = server.linkServer(token, userId)
        if (result != null) {
            channel.createMessage("La liaison a été effectuée.").awaitFirst()
        } else {
            channel.createMessage("La liaison n'a pas pu être effectuée.").awaitFirst()
        }
    }
}
