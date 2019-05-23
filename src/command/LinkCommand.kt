package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Configuration

class LinkCommand(
    override val prefix: String
) : TextCommand {

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