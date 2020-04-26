package fr.spoutnik87

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.ContentPlayer
import fr.spoutnik87.bot.ContentPlayerState
import fr.spoutnik87.bot.GetState
import fr.spoutnik87.bot.Server
import fr.spoutnik87.command.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object BotApplication {

    private var started = false

    private val serverList = ConcurrentHashMap<String, Server>()

    private val commandList = ConcurrentHashMap<String, TextCommand>()

    val playerManager = DefaultAudioPlayerManager()

    private val logger = LoggerFactory.getLogger(BotApplication.javaClass)

    lateinit var discordClient: DiscordClient
    private var gatewayDiscordClient: GatewayDiscordClient? = null

    suspend fun start() {
        if (started) {
            return
        }
        logger.info("Application is starting with the following configuration :")
        logger.info("Discord bot token : " + Configuration.token)
        logger.info("Files path : " + Configuration.filesPath)
        logger.info("API URL : " + Configuration.apiUrl)
        logger.info("API username : " + Configuration.username)
        logger.info("API password : " + Configuration.password)
        started = true
        logger.info("BotApplication is starting")
        if (Configuration.restApi) {
            RestClient.loadToken()
        }
        loadCommands()

        discordClient = DiscordClientBuilder.create(Configuration.token).build()

        playerManager.configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        GlobalScope.launch {
            discordClient.gateway().withGateway { client ->
                gatewayDiscordClient = client
                GlobalScope.launch {
                    client.eventDispatcher.on(MessageCreateEvent::class.java).asFlow().onEach { event ->
                        if (!event.message.content.isNullOrEmpty() && event.guildId.isPresent) {
                            val content = event.message.content
                            val guildId = event.guildId.get().asString()
                            val server = serverList[guildId]
                            if (server != null) {
                                commandList.filter { content.startsWith(Configuration.superPrefix + it.key) }
                                    .forEach {
                                        try {
                                            it.value.execute(event, server)
                                        } catch (e: Exception) {
                                            logger.error(
                                                "An error happened during command processing : $content",
                                                e
                                            )
                                        }
                                    }
                            }
                        }
                    }.collect()
                }


                GlobalScope.launch {
                    client.eventDispatcher.on(VoiceStateUpdateEvent::class.java).asFlow().onEach {
                        val server = getServer(it.current.guildId.asString())
                        val bot = getServer(it.current.guildId.asString())?.bot
                        if (server != null && bot != null) {
                            bot.voiceStates[it.current.userId] = it.current
                            // If the bot is playing, check if the bot is alone.
                            val response = CompletableDeferred<ContentPlayerState>()
                            server.player.send(GetState(response))
                            val state = response.await()
                            if (state.playing && bot.currentChannelId != null) {
                                // Get number of people in the same channel as the bot.
                                val people =
                                    bot.voiceStates.filter { it1 -> it1.value.channelId.isPresent }.map { it1 ->
                                        it1.value.channelId.get().asString()
                                    }.filter { it1 -> it1 == bot.currentChannelId }.size
                                // If the bot is alone, the bot leave the channel.
                                if (people < 2) {
                                    logger.info("The bot is alone in a channel on server : ${server.guild.id.asString()}. It is now leaving the channel.")
                                    server.clearContents()
                                }
                            }
                        }
                    }.collect()
                }

                GlobalScope.launch {
                    client.guilds.asFlow().onEach {
                        if (it is Guild && serverList[it.id.asString()] == null) {
                            logger.debug("Server with id ${it.id.asString()} is initializing")
                            val server =
                                Server(it, ContentPlayer(it.id.asString(), playerManager.createPlayer()))
                            server.init()
                            serverList[it.id.asString()] = server
                        } else {
                            logger.error("An error happened during server with id ${it.id.asString()} initialization")
                        }
                    }.collect()
                }

                return@withGateway client.onDisconnect()
            }.block()

        }
        GlobalScope.launch {
            delay(1000)
            discordClient.login().block()
        }


    }

    private fun loadCommands() {
        commandList["help"] = HelpCommand("help")
        commandList["link"] = LinkCommand("link")
        commandList["join"] = JoinCommand("join")
        commandList["list"] = PlaylistTextCommand("list")
        commandList["play"] = PlayTextCommand("play")
        commandList["stop"] = StopTextCommand("stop")
        commandList["skip"] = SkipTextCommand("skip")
        commandList["resume"] = ResumeTextCommand("resume")
        commandList["pause"] = PauseTextCommand("pause")
        commandList["replay"] = ReplayTextCommand("replay")
        commandList["force"] = ForceTextCommand("force")
        commandList["pos"] = SetPositionTextCommand("pos")
        commandList["dev"] = DevTextCommand("dev")
        commandList["enable"] = EnableFeatureTextCommand("enable")
        commandList["disable"] = DisableFeatureTextCommand("disable")
    }

    fun getServer(guildId: String) = serverList[guildId]

    suspend fun stop() {
        if (!started) {
            return
        }
        logger.debug("BotApplication is stopping")
        started = false
        gatewayDiscordClient?.logout()?.awaitFirst()
    }
}
