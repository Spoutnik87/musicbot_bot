package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Command
import fr.spoutnik87.DiscordBot

class HelpCommand(
    override val prefix: String,
    override val discordBot: DiscordBot
) : Command {

    override suspend fun execute(messageEvent: MessageCreateEvent) {
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