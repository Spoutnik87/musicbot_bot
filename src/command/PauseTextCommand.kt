package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Server
import fr.spoutnik87.bot.TextCommand

class PauseTextCommand(override val prefix: String) : TextCommand {
    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        server.pauseContent()
    }
}