package fr.spoutnik87

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

interface Command {

    val prefix: String
    val discordBot: DiscordBot

    fun execute(message: MessageCreateEvent): Mono<Unit>
}