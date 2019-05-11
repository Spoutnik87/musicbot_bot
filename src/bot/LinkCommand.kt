package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Command
import fr.spoutnik87.DiscordBot
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono

class LinkCommand(
    override val prefix: String,
    override val discordBot: DiscordBot
) : Command {

    override fun execute(messageEvent: MessageCreateEvent): Mono<Unit> {
        return Mono.justOrEmpty(messageEvent.message.content).map { it.substring(prefix.length + 1) }.doOnNext {
            if (messageEvent.guildId.isPresent) {
                val guildId = messageEvent.guildId.get().toString()
                runBlocking {
                    launch {
                        discordBot.serverList[guildId]?.linkServer(it)
                    }
                }
            }
        }.flatMap { Mono.empty<Unit>() }
    }
}