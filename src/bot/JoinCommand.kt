package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Command
import fr.spoutnik87.DiscordBot

class JoinCommand(
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
        val userId = messageEvent.message.author.get().id.asString()
        val guildId = messageEvent.guildId.get().asString()
        val channel = messageEvent.message.channel.block()
        if (discordBot.serverList[guildId] == null) {
            channel.createMessage("Une erreur est survenue.").block()
            return
        }

        var status = discordBot.musicbotRestClient.joinServer(userId, guildId, token)
        if (status != null) {
            channel.createMessage("Vous avez rejoint ce serveur.").block()
        } else {
            channel.createMessage("Votre demande pour rejoindre ce serveur n'a pas pu être prise en compte.").block()
        }
    }
}