package fr.spoutnik87.bot

import com.beust.klaxon.Klaxon
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.Guild
import fr.spoutnik87.DiscordBot
import fr.spoutnik87.model.MusicbotRestDecodedLinkToken
import fr.spoutnik87.model.MusicbotRestServerModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils

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
        runBlocking {
            launch {
                loadServerData()
            }
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
        val server = discordBot.musicbotRestClient.getServerByGuildId(guild.id)
        if (server == null) {
            linkable = true
            initialized = false
        } else {
            this.server = server
            linkable = !server.linked
            initialized = true
        }
    }

    suspend fun linkServer(base64encodedLinkToken: String) {
        try {
            val decodedLinkToken = Klaxon().parse<MusicbotRestDecodedLinkToken>(
                StringUtils.newStringUtf8(
                    Base64.decodeBase64(base64encodedLinkToken)
                )
            ) ?: return
            val server = discordBot.musicbotRestClient.linkGuildToServer(
                decodedLinkToken.serverId,
                guild.id.toString(),
                decodedLinkToken.token
            ) ?: return
            this.server = server
        } catch (e: Exception) {
            println("An error happened during an attempt to link Guild ${guild.id} to a server.")
        }
    }
}