package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.Guild
import fr.spoutnik87.DiscordBot
import fr.spoutnik87.model.MusicbotRestServerModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Server(
    val discordBot: DiscordBot,
    val guild: Guild,
    val player: AudioPlayer
) {

    private var server: MusicbotRestServerModel? = null
    val audioProvider = LavaPlayerAudioProvider(player)
    var initialized = false
    var linkable = false

    private var currentTrack: AudioTrack? = null

    val isPlayingTrack: Boolean
        get() = player.playingTrack != null

    val trackStartTime: Long? = null

    init {
        GlobalScope.launch {
            loadServerData()
        }
        player.addListener {
            if (it is TrackStartEvent) {
                onTrackStart()
            } else if (it is TrackEndEvent) {
                onTrackEnd()
            }
        }
    }

    fun onTrackStart() {
        println("Track started")
    }

    fun onTrackEnd() {
        println("Track ended")
    }

    suspend fun loadServerData() {
        val server = discordBot.musicbotRestClient.getServerByGuildId(guild.id.asString())
        if (server == null) {
            linkable = true
            initialized = false
        } else {
            this.server = server
            linkable = !server.linked
            initialized = true
        }
    }

    suspend fun linkServer(linkToken: String, userId: String) {
        try {
            val server = discordBot.musicbotRestClient.linkGuildToServer(
                userId,
                guild.id.asString(),
                linkToken
            ) ?: return
            this.server = server
            this.linkable = false
        } catch (e: Exception) {
            println("An error happened during an attempt to link Guild ${guild.id.asString()} to a server.")
        }
    }

    suspend fun joinServer(joinToken: String, userId: String) {
        try {
            discordBot.musicbotRestClient.joinServer(
                userId,
                guild.id.asString(),
                joinToken
            ) ?: return
        } catch (e: Exception) {
            println("An error happened during an attempt of user $userId to join Guild ${guild.id.asString()}.")
        }
    }
}