package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.Snowflake
import discord4j.voice.VoiceConnection
import fr.spoutnik87.BotApplication
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class Bot(
    private val server: Server
) {
    private val audioProvider = LavaPlayerAudioProvider(server.player)

    private var voiceConnection: VoiceConnection? = null
    var currentChannelId: String? = null

    val playingTrack
        get() = server.player.playingTrack

    val isPlayingTrack
        get() = playingTrack != null

    var track: AudioTrack? = null

    private var pausedPosition: Long? = null
    val isPaused
        get() = pausedPosition != null

    private var fireStopTrackEvent = true

    var voiceStates = ConcurrentHashMap<Snowflake, VoiceState>()

    init {
        server.player.addListener {
            GlobalScope.launch {
                if (it is TrackStartEvent) {
                    server.onContentStart()
                } else if (it is TrackEndEvent) {
                    if (fireStopTrackEvent) {
                        server.onContentStop()
                    } else {
                        fireStopTrackEvent = true
                    }
                }
            }
        }
    }

    suspend fun canJoin(userId: String) = canJoin(Snowflake.of(userId))

    suspend fun canJoin(userId: Snowflake): Boolean {
        val voiceState = voiceStates[userId] ?: return false
        if (!voiceState.channelId.isPresent) {
            return false
        }
        val channel = voiceState.channel.block() ?: return false
        val member = BotApplication.client.getMemberById(server.guild.id, userId).block() ?: return false
        val permissions = channel.getEffectivePermissions(member.id).block()?.asEnumSet() ?: return false
        return permissions.contains(Permission.CONNECT) && permissions.contains(Permission.SPEAK)
    }

    suspend fun joinVoiceChannel(userId: Snowflake): Boolean {
        if (currentChannelId != null) {
            leaveVoiceChannel()
        }
        val voiceState = voiceStates[userId] ?: return false
        val channel = voiceState.channel.block() ?: return false
        voiceConnection = channel.join {
            it.setProvider(audioProvider)
        }.block() ?: return false
        currentChannelId = channel.id.asString()
        return true
    }

    suspend fun joinVoiceChannel(userId: String) = joinVoiceChannel(Snowflake.of(userId))

    fun leaveVoiceChannel() {
        if (voiceConnection != null) {
            voiceConnection?.disconnect()
            voiceConnection = null
        }
        currentChannelId = null
    }

    /**
     * Start a track. Must be in a voice channel.
     */
    fun playTrack(audioTrack: AudioTrack) {
        if (isPlayingTrack) {
            stopTrackWithoutEvent()
        }
        pausedPosition = null
        track = audioTrack.makeClone()
        server.player.playTrack(audioTrack)
    }

    /**
     * Stop playing track
     */
    fun stopTrack() {
        server.player.stopTrack()
    }

    /**
     * Stop playing track without firing stop event.
     */
    @Synchronized
    fun stopTrackWithoutEvent() {
        fireStopTrackEvent = false
        server.player.stopTrack()
    }

    /**
     * Update track position
     * @param position Position in millis
     */
    fun setTrackPosition(position: Long): Boolean {
        if (!isPlayingTrack && !isPaused) {
            return false
        }
        val track = this.track ?: return false
        if (isPaused) {
            pausedPosition = position
            track.position = position
            server.playingContent?.loadTrack(track)
        } else {
            stopTrackWithoutEvent()
            track.position = position
            playTrack(track)
            server.playingContent?.loadTrack(track)
        }
        return true
    }

    /**
     * Pause currently playing track.
     */
    fun pausePlayingTrack() {
        if (!isPlayingTrack) {
            return
        }
        if (isPaused) {
            return
        }
        pausedPosition = playingTrack?.position
        stopTrackWithoutEvent()
    }

    /**
     * Play paused track with it's old position.
     */
    fun resumePlayingTrack() {
        if (server.playingContent == null || !isPaused) {
            return
        }
        track?.position = pausedPosition ?: return
        playTrack(track ?: return)
    }

    fun reset() {
        track = null
        pausedPosition = null
    }
}