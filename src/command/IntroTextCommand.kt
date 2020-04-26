package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Configuration
import fr.spoutnik87.bot.FileContent
import fr.spoutnik87.bot.Server
import fr.spoutnik87.feature.IntroFeature
import fr.spoutnik87.util.UUID
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory

class IntroTextCommand(override val prefix: String) : TextCommand {

    private val logger = LoggerFactory.getLogger(EnableFeatureTextCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        val channel = messageEvent.message.channel.awaitFirst() ?: return
        if (!messageEvent.member.isPresent) return
        val user = messageEvent.member.get()
        val options = messageEvent.message.content.get().split(" ").filter { it != "" }
        if (options.size < 2) return
        val intros = IntroFeature.loadIntroFile()
        intros.filter { introItem ->
            introItem.username == options[1]
        }.takeIf { it.isNotEmpty() }?.random()?.also {
            server.playContent(
                FileContent(
                    UUID.v4(),
                    UUID.v4(),
                    user.id.asString(),
                    "${Configuration.resources_path}/${it.intro}",
                    0,
                    "Intro ${options[1]}"
                )
            )
        }
        channel.createMessage("Action effectuée").awaitFirst()
    }
}
