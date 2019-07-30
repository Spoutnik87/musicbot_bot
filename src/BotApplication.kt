package fr.spoutnik87

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.ContentPlayer
import fr.spoutnik87.bot.Server
import fr.spoutnik87.command.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object BotApplication {

    lateinit var client: DiscordClient
    private var started = false

    private val serverList = ConcurrentHashMap<String, Server>()

    private val commandList = ConcurrentHashMap<String, TextCommand>()

    val playerManager = DefaultAudioPlayerManager()

    private val logger = LoggerFactory.getLogger(BotApplication.javaClass)

    fun start() {
        if (started) {
            return
        }
        started = true
        logger.debug("BotApplication is starting")
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
            val server = getServer(it.current.guildId.asString())
            val bot = getServer(it.current.guildId.asString())?.bot
            if (server != null && bot != null) {
                bot.voiceStates[it.current.userId] = it.current
                // If the bot is playing, check if the bot is alone.
                if (server.player.isPlaying() && bot.currentChannelId != null) {
                    // Get number of people in the same channel as the bot.
                    val people = bot.voiceStates.filter { it1 -> it1.value.channelId.isPresent }.map { it1 ->
                        it1.value.channelId.get().toString()
                    }.filter { it1 -> it1 == bot.currentChannelId }.size
                    // If the bot is alone, the bot leave the channel.
                    if (people < 2) {
                        GlobalScope.launch {
                            server.clearContents()
                        }
                    }
                }
            }
        }.subscribe()
        playerManager.configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        client.guilds.subscribe {
            if (it is Guild && serverList[it.id.asString()] == null) {
                logger.debug("Server with id ${it.id.asString()} is initializing")
                serverList[it.id.asString()] = Server(it, ContentPlayer(playerManager.createPlayer()))
            } else {
                logger.error("An error happened during server with id ${it.id.asString()} initialization")
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
        commandList["playlist"] = PlaylistTextCommand("playlist")
        commandList["play"] = PlayTextCommand("play")
        commandList["stop"] = StopTextCommand("stop")
        commandList["skip"] = SkipTextCommand("skip")
        commandList["resume"] = ResumeTextCommand("resume")
        commandList["pause"] = PauseTextCommand("pause")
        commandList["replay"] = ReplayTextCommand("replay")
        commandList["force"] = ForceTextCommand("force")
        commandList["setpos"] = SetPositionTextCommand("setpos")
        commandList["dev"] = DevTextCommand("dev")
    }

    fun getServer(guildId: String) = serverList[guildId]

    fun stop() {
        if (!started) {
            return
        }
        logger.debug("BotApplication is stopping")
        started = false
        client.logout().subscribe()
    }
}