package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Command
import fr.spoutnik87.DiscordBot

class PlayCommand(
    override val prefix: String,
    override val discordBot: DiscordBot
) : Command {

    override suspend fun execute(messageEvent: MessageCreateEvent) {
        if (!messageEvent.message.content.isPresent
            || messageEvent.message.content.get().length <= DiscordBot.SUPER_PREFIX.length + prefix.length + 1
        ) {
            return
        }
        val url = messageEvent.message.content.get().substring(DiscordBot.SUPER_PREFIX.length + prefix.length + 1)
        if (!messageEvent.guildId.isPresent || !messageEvent.message.author.isPresent) {
            return
        }
        val userId = messageEvent.message.author.get().id.asString()
        val guildId = messageEvent.guildId.get().asString()
        val channel = messageEvent.message.channel.block() ?: return

        if (!messageEvent.member.isPresent) {
            return
        }
        val member = messageEvent.member.get()
        val voiceState = member.voiceState.block() ?: return
        val voiceChannel = voiceState.channel.block() ?: return
        val server = discordBot.serverList[guildId] ?: return

        server.voiceConnection = voiceChannel.join {
            it.setProvider(server.audioProvider)
        }.block()

        // val scheduler = TrackScheduler(server)
        // discordBot.playerManager.loadItem("https://www.youtube.com/watch?v=JP4Ar1vscGE", scheduler)
    }
}