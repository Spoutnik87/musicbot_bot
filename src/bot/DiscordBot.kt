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

class DiscordBot(
    val configuration: Configuration
) {

    lateinit var client: DiscordClient
    private var started = false

    val musicbotRestClient = MusicbotRestClient(this)
    val serverList = HashMap<String, Server>()

    val commandList = HashMap<String, Command>()

    val playerManager = DefaultAudioPlayerManager()

    fun start() {
        if (started) {
            return
        }
        started = true

        loadCommands()

        client = DiscordClientBuilder(configuration.token).build()
        client.eventDispatcher.on(MessageCreateEvent::class.java).doOnNext { event ->
            GlobalScope.launch {
                if (event.message.content.isPresent) {
                    val content = event.message.content.get()
                    commandList.filter { content.startsWith(SUPER_PREFIX + it.key) }.forEach { it.value.execute(event) }
                }
            }
        }.subscribe()
        client.eventDispatcher.on(VoiceStateUpdateEvent::class.java).doOnNext {

        }.subscribe()

        playerManager.configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        client.guilds.subscribe {
            if (it is Guild && serverList[it.id.asString()] == null) {
                serverList[it.id.asString()] = Server(this, it, playerManager.createPlayer())
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
        commandList["help"] = HelpCommand("help", this)
        commandList["link"] = LinkCommand("link", this)
        commandList["join"] = JoinCommand("join", this)
        commandList["play"] = PlayCommand("play", this)
    }

    fun stop() {
        if (!started) {
            return
        }
        started = false
        client.logout().subscribe()
    }

    companion object {
        const val SUPER_PREFIX = "!!"
    }
}