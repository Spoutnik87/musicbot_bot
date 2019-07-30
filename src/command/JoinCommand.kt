package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Configuration
import fr.spoutnik87.RestClient
import fr.spoutnik87.bot.Server
import org.slf4j.LoggerFactory

class JoinCommand(
    override val prefix: String
) : TextCommand {

    private val logger = LoggerFactory.getLogger(JoinCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        if (!messageEvent.message.content.isPresent
            || messageEvent.message.content.get().length <= Configuration.superPrefix.length + prefix.length + 1
        ) {
            return
        }
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val token = messageEvent.message.content.get().substring(Configuration.superPrefix.length + prefix.length + 1)
        val userId = messageEvent.message.author.get().id.asString()
        val channel = messageEvent.message.channel.block() ?: return
        // messageEvent.message.delete().block()

        var status = RestClient.joinServer(userId, server.guild.id.asString(), token)
        if (status != null) {
            channel.createMessage("Vous avez rejoint ce serveur.").block()
        } else {
            channel.createMessage("Votre demande pour rejoindre ce serveur n'a pas été accepté. Vous êtes peut-être déjà membre de ce serveur.")
                .block()
        }
    }
}