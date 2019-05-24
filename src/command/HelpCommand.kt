package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent

class HelpCommand(
    override val prefix: String
) : TextCommand {

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        val channel = messageEvent.message.channel.block() ?: return
        channel.createMessage(
            """
            Musicbot -- HELP
            Command link : TODO
            Command join : TODO
            """.trimIndent()
        ).block()
    }
}