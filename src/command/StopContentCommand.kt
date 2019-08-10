package fr.spoutnik87.command

import fr.spoutnik87.bot.ContentPlayerState
import fr.spoutnik87.bot.GetState
import fr.spoutnik87.bot.Server
import fr.spoutnik87.event.WebRequestEvent
import fr.spoutnik87.reader.StopContentReader
import kotlinx.coroutines.CompletableDeferred
import org.slf4j.LoggerFactory

class StopContentCommand : WebCommand {

    private val logger = LoggerFactory.getLogger(StopContentCommand::class.java)

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val reader = event.payload as StopContentReader
        val response = CompletableDeferred<ContentPlayerState>()
        server.player.send(GetState(response))
        val state = response.await()
        if (reader.uid == state.content?.uid) {
            server.stopPlayingContent()
            server.playNextContent()
        } else {
            server.queue.removeContent(reader.uid)
        }
    }
}