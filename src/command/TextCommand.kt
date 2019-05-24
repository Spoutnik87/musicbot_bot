package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Command

interface TextCommand : Command {

    val prefix: String

    suspend fun execute(messageEvent: MessageCreateEvent, server: Server)
}