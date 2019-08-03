package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.RestClient
import fr.spoutnik87.bot.Server
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory

class JoinCommand(
    override val prefix: String
) : TextCommand {

    private val logger = LoggerFactory.getLogger(JoinCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        if (!messageEvent.message.content.isPresent) {
            return
        }
        val options = messageEvent.message.content.get().split(" ").filter { it != "" }
        if (options.size < 2) return;
        val token = options[1]
        val userId = messageEvent.message.author.get().id.asString()
        val channel = messageEvent.message.channel.awaitFirst() ?: return
        // messageEvent.message.delete().block()

        var status = RestClient.joinServer(userId, server.guild.id.asString(), token)
        if (status != null) {
            channel.createMessage("Vous avez rejoint ce serveur.").awaitFirst()
        } else {
            channel.createMessage("Votre demande pour rejoindre ce serveur n'a pas été accepté. Vous êtes peut-être déjà membre de ce serveur.")
                .awaitFirst()
        }
    }
}