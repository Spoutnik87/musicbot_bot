package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Command
import fr.spoutnik87.DiscordBot

class LinkCommand(
    override val prefix: String,
    override val discordBot: DiscordBot
) : Command {

    override suspend fun execute(messageEvent: MessageCreateEvent) {
        if (!messageEvent.message.content.isPresent
            || messageEvent.message.content.get().length <= DiscordBot.SUPER_PREFIX.length + prefix.length + 1
        ) {
            return
        }
        val token = messageEvent.message.content.get().substring(DiscordBot.SUPER_PREFIX.length + prefix.length + 1)
        if (!messageEvent.guildId.isPresent || !messageEvent.message.author.isPresent) {
            return
        }
        val userId = messageEvent.message.author.get().id.asString()
        val guildId = messageEvent.guildId.get().asString()
        val channel = messageEvent.message.channel.block() ?: return
        // messageEvent.message.delete().block()
        if (discordBot.serverList[guildId] == null) {
            channel.createMessage("Une erreur est survenue.").block()
            return
        }
        if (discordBot.serverList[guildId]?.linkable == false) {
            channel.createMessage("Ce serveur est déjà lié.").block()
            return
        }

        val result = discordBot.serverList[guildId]?.linkServer(token, userId)
        if (result != null) {
            channel.createMessage("La liaison a été effectuée.").block()
        } else {
            channel.createMessage("La liaison n'a pas pu être effectuée.").block()
        }
    }
}