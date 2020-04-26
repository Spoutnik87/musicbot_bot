package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Server
import fr.spoutnik87.util.Utils
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory

class SetPositionTextCommand(override val prefix: String) : TextCommand {

    private val logger = LoggerFactory.getLogger(SetPositionTextCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        if (messageEvent.message.content.isNullOrEmpty()) return
        val channel = messageEvent.message.channel.awaitFirst() ?: return
        val options = messageEvent.message.content.split(" ").filter { it != "" }
        val positionString = options.getOrNull(1)
        val position = if (positionString?.contains(":") == true) {
            Utils.fromDurationString(positionString)
        } else {
            positionString?.toLongOrNull()?.let { it * 1000 }
        }
        position?.apply {
            server.setContentPosition(this)
            channel.createMessage("Action effectuée").awaitFirst()
        }
    }
}
