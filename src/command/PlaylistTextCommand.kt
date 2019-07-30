package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Content
import fr.spoutnik87.bot.Server
import fr.spoutnik87.util.Utils
import org.slf4j.LoggerFactory

class PlaylistTextCommand(override val prefix: String) : TextCommand {

    private val logger = LoggerFactory.getLogger(PlaylistTextCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        val channel = messageEvent.message.channel.block() ?: return
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        var queue = ""
        server.queue.getAllContents().map { formatContent(it) }.forEach { queue = queue.plus(it).plus("\n") }

        channel.createMessage(
            """----------------------------- Musicbot playlist -----------------------------
Morceau actuellement joué :
${formatContent(server.player.getPlayingContent(), server.player.getPosition())}
Liste de la file d'attente :
$queue
-------------------------------------------------------------------------""".trimIndent()
        ).block()
    }

    private fun formatContent(content: Content?, position: Long? = null) =
        content?.let {
            """+------------------------
${formatContentTopLine(content.id, content.name)}
${formatContentBottomLines(
                content.id,
                position?.let { Utils.toDurationString(it) },
                content.duration?.let { Utils.toDurationString(it) } ?: "")}
+------------------------""".trimIndent()
        } ?: ""


    /**
     * @param name Content name
     */
    private fun formatContentTopLine(id: String?, name: String?): String {
        return if (id != null) {
            "| $name"
        } else {
            name?.let { "| $it" } ?: "|"
        }
    }


    /**
     * @param uid Content uuid
     * @param link Youtube link
     * @param duration
     */
    private fun formatContentBottomLines(id: String?, position: String?, duration: String?): String {
        var result = ""
        result += "| ${position?.let { it } ?: "00:00"} -- ${duration?.let { it } ?: "00:00"}"
        if (id != null) {
            result += "\n| https://musicbot.spout.cc/content/$id"
        }
        return result
    }
}