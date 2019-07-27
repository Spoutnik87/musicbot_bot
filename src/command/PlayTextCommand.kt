package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Content
import fr.spoutnik87.bot.Server
import fr.spoutnik87.bot.TextCommand
import fr.spoutnik87.util.URLHelper
import fr.spoutnik87.util.UUID
import fr.spoutnik87.util.Utils

class PlayTextCommand(
    override val prefix: String
) : TextCommand {

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        if (!messageEvent.message.content.isPresent
            || !messageEvent.message.author.isPresent
        ) return
        val options = messageEvent.message.content.get().split(" ")
        if (options.size < 2) return
        val link = URLHelper.createSafeYoutubeLink(options[1])
        if (link != null) {
            val userId = messageEvent.message.author.get().id.asString()
            val positionString = options.getOrNull(2)
            val position = if (positionString?.contains(":") == true) {
                Utils.fromDurationString(positionString)
            } else {
                positionString?.toLongOrNull()?.let { it * 1000 }
            }
            Utils.loadMetadata(link)?.let {
                server.playContent(Content(UUID.v4(), null, userId, link, position, it.title, it.duration))
            }
        }
    }
}