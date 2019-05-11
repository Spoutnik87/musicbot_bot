package fr.spoutnik87

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.LinkCommand
import fr.spoutnik87.bot.Server
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class DiscordBot(
    val configuration: Configuration
) {

    private lateinit var client: DiscordClient
    private var started = false

    val musicbotRestClient = MusicbotRestClient(this)
    val serverList = HashMap<String, Server>()

    val commandList = HashMap<String, Command>()

    fun start() {
        if (started) {
            return
        }
        started = true

        loadCommands()

        client = DiscordClientBuilder(configuration.token).build()
        client.eventDispatcher.on(MessageCreateEvent::class.java)
            .flatMap { event ->
                Mono.justOrEmpty(event.message.content)
                    .flatMap { content ->
                        Flux.fromIterable(commandList.entries)
                            .filter { content.startsWith("!!" + it.key) }
                            .flatMap { it.value.execute(event) }
                            .next()
                    }
            }
            .subscribe()

        val playerManager = DefaultAudioPlayerManager()
        playerManager.configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
        AudioSourceManagers.registerRemoteSources(playerManager)

        client.login().subscribe()

        client.guilds.subscribe {

            if (it is Guild && serverList[it.id.toString()] == null) {
                serverList[it.id.toString()] = Server(this, it, playerManager.createPlayer())
            } else {
                System.out.println("An error happened during Guild loading")
            }
        }
    }

    private fun loadCommands() {
        commandList["link"] = LinkCommand("link", this)
    }

    fun stop() {
        if (!started) {
            return
        }
        started = false
        client.logout().subscribe()
    }
}