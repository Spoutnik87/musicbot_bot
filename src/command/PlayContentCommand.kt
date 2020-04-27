package fr.spoutnik87.command

import fr.spoutnik87.bot.IdContent
import fr.spoutnik87.bot.Server
import fr.spoutnik87.bot.YoutubeContent
import fr.spoutnik87.event.WebRequestEvent
import fr.spoutnik87.reader.PlayContentReader
import org.slf4j.LoggerFactory

class PlayContentCommand : WebCommand {

    private val logger = LoggerFactory.getLogger(PlayContentCommand::class.java)

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val reader = event.payload as PlayContentReader
        val content = if (reader.link != null) {
            YoutubeContent(
                reader.uid,
                reader.id,
                reader.initiator,
                reader.link,
                null,
                reader.name,
                reader.duration
            )
        } else {
            IdContent(
                reader.uid,
                reader.id,
                reader.initiator,
                null,
                reader.name,
                reader.duration
            )
        }
        server.playContent(content)
    }
}
