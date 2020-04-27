package fr.spoutnik87.feature

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import discord4j.core.event.domain.VoiceStateUpdateEvent
import fr.spoutnik87.BotApplication
import fr.spoutnik87.Configuration
import fr.spoutnik87.bot.FileContent
import fr.spoutnik87.bot.Server
import fr.spoutnik87.util.UUID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class IntroItem(val username: String, val intro: String)

class IntroFeature(val server: Server) : Feature {
    private var job: Job? = null
    private var lastPlayDate = ConcurrentHashMap<String, Date>()

    override suspend fun start() {
        job = GlobalScope.launch {
            BotApplication.client.eventDispatcher.on(VoiceStateUpdateEvent::class.java).asFlow().onEach { voiceState ->
                if (!voiceState.old.isPresent && voiceState.current.channelId.isPresent) {
                    val intros = loadIntroFile()
                    val user = voiceState.current.user.awaitFirst()
                    intros.filter { introItem ->
                        introItem.username == user.username
                    }.takeIf { it.isNotEmpty() }?.random()?.also {
                        if (lastPlayDate[it.username]?.after(Date(Date().time - 1000 * 15 * 60)) == true) {
                            return@also
                        }
                        lastPlayDate[it.username] = Date()
                        server.playContent(
                            FileContent(
                                UUID.v4(),
                                UUID.v4(),
                                user.id.asString(),
                                "${Configuration.resources_path}${it.intro}",
                                0,
                                "Intro ${user.username}"
                            )
                        )
                    }
                }
            }.collect()
        }
    }

    override suspend fun stop() {
        job?.cancel()
        lastPlayDate.clear()
    }

    companion object {
        fun loadIntroFile(): List<IntroItem> {
            return jacksonObjectMapper().readValue(
                File("${Configuration.resources_path}intro.json").bufferedReader().readText()
            )
        }
    }
}
