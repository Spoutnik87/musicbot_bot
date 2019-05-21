package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.util.Snowflake
import discord4j.voice.VoiceConnection
import fr.spoutnik87.DiscordBot
import fr.spoutnik87.model.ContentViewModel
import fr.spoutnik87.model.MusicbotRestServerModel
import fr.spoutnik87.model.QueueViewModel
import fr.spoutnik87.viewmodel.ServerViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Server(
    val discordBot: DiscordBot,
    val guild: Guild,
    val player: AudioPlayer
) {

    private var server: MusicbotRestServerModel? = null
    val audioProvider = LavaPlayerAudioProvider(player)
    var voiceConnection: VoiceConnection? = null

    var initialized = false
    var linkable = false

    val isPlayingTrack: Boolean
        get() = player.playingTrack != null

    val queue = Queue(this)

    var currentChannelId: String? = null

    init {
        GlobalScope.launch {
            loadServerData()
        }
        player.addListener {
            GlobalScope.launch {
                if (it is TrackStartEvent) {
                    onTrackStart()
                } else if (it is TrackEndEvent) {
                    onTrackEnd()
                }
            }
        }
    }

    suspend fun onTrackLoad() {
        if (player.playingTrack != null) {
            return
        }
        val content = queue.next() ?: return
        joinChannel(content.initiator)
        player.playTrack(content.audioTrack)
    }

    suspend fun onTrackStart() {
        println("Track started")
    }

    suspend fun onTrackEnd() {
        println("Track ended")
        val content = queue.next()
        if (content?.audioTrack == null) {
            queue.stop()
            leaveChannel()
            return
        }
        player.playTrack(content.audioTrack)
    }

    suspend fun joinChannel(userId: String): Boolean {
        if (currentChannelId != null) {
            leaveChannel()
        }
        val user = discordBot.client.getUserById(Snowflake.of(userId)).block()
        val member = user?.asMember(guild.id)?.block() ?: return false
        val voiceState = member.voiceState.block()
        val channel = voiceState?.channel?.block() ?: return false
        voiceConnection = channel.join {
            it.setProvider(audioProvider)
        }.block() ?: return false
        currentChannelId = channel.id.asString()
        return true
    }

    suspend fun leaveChannel() {
        if (voiceConnection != null) {
            voiceConnection?.disconnect()
            voiceConnection = null
        }
        currentChannelId = null
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

    suspend fun linkServer(linkToken: String, userId: String): MusicbotRestServerModel? {
        try {
            val server = discordBot.musicbotRestClient.linkGuildToServer(
                userId,
                guild.id.asString(),
                linkToken
            ) ?: return null
            this.server = server
            this.linkable = false
            return server
        } catch (e: Exception) {
            println("An error happened during an attempt to link Guild ${guild.id.asString()} to a server.")
        }
        return null
    }

    suspend fun joinServer(joinToken: String, userId: String): Any? {
        try {
            return discordBot.musicbotRestClient.joinServer(
                userId,
                guild.id.asString(),
                joinToken
            ) ?: return null
        } catch (e: Exception) {
            println("An error happened during an attempt of user $userId to join Guild ${guild.id.asString()}.")
        }
        return null
    }

    fun addTrack(id: String, initiator: String) {
        val scheduler = TrackScheduler(this, id, initiator)
        discordBot.playerManager.loadItem(discordBot.configuration.filesPath + "media\\" + id, scheduler)
    }

    fun removeTrack(id: String, initiator: String) {
        if (queue.currentlyPlaying?.id == id) {
            player.stopTrack()
        } else if (queue.contains(id)) {
            queue.remove(id)
        }
    }

    fun clearTracks(initiator: String) {
        player.stopTrack()
        queue.clear()
    }

    fun updateTrackPosition(id: String, initiator: String, position: Long) {
        if (queue.currentlyPlaying?.id == id) {
            // TODO Reload track with new position.
            player.playingTrack?.position = position
        }
    }

    fun getStatus(): ServerViewModel {
        return ServerViewModel(guild.id.asString(), QueueViewModel(queue.getAllContents()
            .map { ContentViewModel(it.id, it.initiator, it.duration, null, null) }), queue.currentlyPlaying.let {
            if (it != null) {
                ContentViewModel(it.id, it.initiator, it.duration, it.startTime, it.position)
            } else {
                null
            }
        })
    }
}