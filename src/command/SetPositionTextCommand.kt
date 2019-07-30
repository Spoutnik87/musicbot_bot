package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Server
import fr.spoutnik87.util.Utils
import org.slf4j.LoggerFactory

class SetPositionTextCommand(override val prefix: String) : TextCommand {

    private val logger = LoggerFactory.getLogger(SetPositionTextCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        if (!messageEvent.message.content.isPresent) return
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val options = messageEvent.message.content.get().split(" ")
        val positionString = options.getOrNull(1)
        val position = if (positionString?.contains(":") == true) {
            Utils.fromDurationString(positionString)
        } else {
            positionString?.toLongOrNull()?.let { it * 1000 }
        }
        position?.apply { server.setContentPosition(this) }
    }
}