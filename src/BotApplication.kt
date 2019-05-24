package fr.spoutnik87

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object BotApplication {

    lateinit var client: DiscordClient
    private var started = false

    private val serverList = ConcurrentHashMap<String, Server>()

    private val commandList = ConcurrentHashMap<String, TextCommand>()

    val playerManager = DefaultAudioPlayerManager()

    fun start() {
        if (started) {
            return
        }
        started = true
        RestClient.loadToken()

        loadCommands()

        client = DiscordClientBuilder(Configuration.token).build()
        client.eventDispatcher.on(MessageCreateEvent::class.java).doOnNext { event ->
            if (event.message.content.isPresent && event.guildId.isPresent) {
                val content = event.message.content.get()
                val guildId = event.guildId.get().asString()
                val server = serverList[guildId]
                if (server != null) {
                    commandList.filter { content.startsWith(Configuration.superPrefix + it.key) }.forEach {
                        GlobalScope.launch {
                            it.value.execute(event, server)
                        }
                    }
                }
            }
        }.subscribe()
        client.eventDispatcher.on(VoiceStateUpdateEvent::class.java).doOnNext {
            val bot = getServer(it.current.guildId.asString())?.bot
            if (bot != null) {
                bot.voiceStates[it.current.userId] = it.current
            }
        }.subscribe()
        playerManager.configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        client.guilds.subscribe {
            if (it is Guild && serverList[it.id.asString()] == null) {
                serverList[it.id.asString()] = Server(it, playerManager.createPlayer())
            } else {
                println("An error happened during Guild loading")
            }
        }
        /**
         * No need to block. This app is alive until Ktor web server is stopped.
         */
        client.login().subscribe()
    }

    private fun loadCommands() {
        commandList["help"] = HelpCommand("help")
        commandList["link"] = LinkCommand("link")
        commandList["join"] = JoinCommand("join")
    }

    fun getServer(guildId: String) = serverList[guildId]

    fun stop() {
        if (!started) {
            return
        }
        started = false
        client.logout().subscribe()
    }
}