package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Server
import fr.spoutnik87.bot.TextCommand

class ReplayTextCommand(override val prefix: String) : TextCommand {
    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        server.setContentPosition(0)
    }
}