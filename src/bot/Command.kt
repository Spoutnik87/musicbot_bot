package fr.spoutnik87

import discord4j.core.event.domain.message.MessageCreateEvent

interface Command {

    val prefix: String
    val discordBot: DiscordBot

    suspend fun execute(message: MessageCreateEvent)
}