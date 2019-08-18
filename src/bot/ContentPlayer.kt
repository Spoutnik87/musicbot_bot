package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import fr.spoutnik87.BotApplication
import fr.spoutnik87.Configuration
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.slf4j.LoggerFactory
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class ContentPlayerAction
data class Play(val content: Content) : ContentPlayerAction()
object Stop : ContentPlayerAction()
object Pause : ContentPlayerAction()
object Resume : ContentPlayerAction()
data class SetPosition(val position: Long) : ContentPlayerAction()
data class GetState(val response: CompletableDeferred<ContentPlayerState>) : ContentPlayerAction()

class ContentPlayer(
    private val audioPlayer: AudioPlayer
) {

    private val logger = LoggerFactory.getLogger(ContentPlayer::class.java)

    val provider = LavaPlayerAudioProvider(audioPlayer)

    private var playingContent: Content? = null

    private var audioTrack: AudioTrack? = null

    private var playingAudioTrack: AudioTrack? = null

    private var startTime: Long? = null

    private val listeners: ArrayList<ContentPlayerListener> = ArrayList()

    private var paused = false

    private var pausing = false

    private var updatingPosition = false

    private var manuallyStopping = false

    private val channel = Channel<Unit>(0)

    private var contentPlayerActor: SendChannel<ContentPlayerAction>? = null

    private fun CoroutineScope.contentPlayerActor() = actor<ContentPlayerAction> {
        for (action in channel) {
            when (action) {
                is Play -> play(action.content)
                is Stop -> stop()
                is Pause -> pause()
                is Resume -> resume()
                is SetPosition -> setPosition(action.position)
                is GetState -> action.response.complete(getState())
            }
        }
    }

    suspend fun init() {
        audioPlayer.addListener {
            GlobalScope.launch {
                onTrackEvent(it)
            }
        }
        GlobalScope.launch {
            contentPlayerActor = contentPlayerActor()
        }
    }

    suspend fun send(action: ContentPlayerAction) {
        this.contentPlayerActor?.send(action)
    }

    /**
     * Asynchronous wrap for load item method.
     */
    private suspend fun loadItem(name: String): AudioTrack? {
        return suspendCoroutine {
            BotApplication.playerManager.loadItem(name, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack?) {
                    logger.debug("AudioPlayer trackLoaded event has been received.")
                    it.resume(track)
                }

                override fun loadFailed(exception: FriendlyException?) {
                    logger.debug("AudioPlayer loadFailed event has been received.")
                    it.resume(null)
                }


                override fun noMatches() {
                    logger.debug("AudioPlayer noMatches event has been received.")
                    it.resume(null)
                }

                override fun playlistLoaded(playlist: AudioPlaylist?) {
                    logger.debug("AudioPlayer playlistLoaded event has been received.")
                    it.resume(null)
                }
            })
        }
    }


    private suspend fun play(content: Content) {
        manualStop()
        clearState()
        this.playingContent = content
        val audioTrack = coroutineScope {
            val item = if (content.link != null) {
                logger.debug("Playing a content with link : ${content.link} and uid : ${content.uid}")
                content.link
            } else {
                logger.debug("Playing a content with id : ${content.id} and uid : ${content.uid}")
                Configuration.filesPath + "media/" + content.id
            }
            loadItem(item)
        }
        if (audioTrack == null) {
            clearState()
            logger.error("Could not load content with id : ${content.id}")
            listeners.forEach {
                it.onContentStartFailure(content)
            }
            return
        }
        this.audioTrack = audioTrack.makeClone()
        this.playingAudioTrack = audioTrack
        logger.debug("A content has been loaded.")
        startTime = System.currentTimeMillis()
        playingAudioTrack?.position = playingContent?.position ?: 0
        audioPlayer.playTrack(playingAudioTrack)
        listeners.forEach {
            it.onContentStart(content)
        }
        logger.debug("Content with uid : ${content.uid} has been started")
    }

    /**
     * Stop the playing content.
     */
    private fun stop() {
        if (isPlaying()) {
            logger.debug("Stopping played content.")
            audioPlayer.stopTrack()
        }
    }

    private suspend fun manualStop() {
        if (isPlaying()) {
            logger.debug("Waiting for AudioPlayer stop event.")
            manuallyStopping = true
            audioPlayer.stopTrack()
            channel.receive()
            logger.debug("AudioPlayer stop event has been executed.")
            manuallyStopping = false
        }
    }

    /**
     * Pause the currently playing content if it's in a playing state.
     */
    private suspend fun pause() {
        if (!isPlaying()) return
        pausing = true
        paused = true
        manualStop()
        pausing = false
        listeners.forEach {
            it.onContentPause(playingContent!!, playingAudioTrack?.position!!)
        }
        logger.debug("Pausing played content.")
    }

    /**
     * Resume the playing content if it's in a paused state.
     */
    private fun resume() {
        if (isPaused()) {
            paused = false
            val pausedPosition = playingAudioTrack?.position
            playingAudioTrack = audioTrack?.makeClone()
            if (pausedPosition != null) {
                playingAudioTrack?.position = pausedPosition
            }
            audioPlayer.playTrack(playingAudioTrack)
            listeners.forEach {
                it.onContentResume(playingContent!!, playingAudioTrack?.position!!)
            }
            logger.debug("Resuming played content.")
        }
    }

    /**
     * Return true if the playing content is paused.
     */
    private fun isPaused(): Boolean {
        return paused;
    }

    /**
     * Set a new position
     */
    private suspend fun setPosition(position: Long) {
        logger.debug("Updating position of played content.")
        if (isPlaying()) {
            var oldPosition: Long?
            if (isPaused()) {
                oldPosition = playingAudioTrack?.position
                playingAudioTrack?.position = position
            } else {
                updatingPosition = true
                manualStop()
                oldPosition = playingAudioTrack?.position
                playingAudioTrack = audioTrack?.makeClone()
                playingAudioTrack?.position = position
                audioPlayer.playTrack(playingAudioTrack)
                updatingPosition = false
            }
            listeners.forEach {
                it.onContentPositionChange(playingContent!!, oldPosition!!, position)
            }
            logger.debug("Updating position of played content.")
        }
    }

    /**
     * Get the current content position or null if there is no content playing.
     */
    private fun getPosition(): Long? {
        return playingAudioTrack?.position
    }

    /**
     * Return the playing content or null if there is no content playing.
     */
    private fun getPlayingContent(): Content? {
        return playingContent
    }

    /**
     * Return the start time of the playing content in millis.
     * Return null if there is no content playing.
     */
    private fun getStartTime(): Long? {
        return startTime
    }

    /**
     * Return true if a content is loaded and playing or paused.
     */
    private fun isPlaying(): Boolean {
        return playingContent != null
    }

    /**
     * Return the current state of the player.
     */
    private fun getState(): ContentPlayerState {
        return ContentPlayerState(playingContent, isPlaying(), isPaused(), getStartTime(), getPosition())
    }

    fun addListener(listener: ContentPlayerListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ContentPlayerListener) {
        listeners.remove(listener)
    }

    private fun clearState() {
        paused = false
        pausing = false
        updatingPosition = false
        playingContent = null
        audioTrack = null
        playingAudioTrack = null
        startTime = null
        logger.debug("ContentPlayer state has been cleared.")
    }

    private suspend fun onTrackEvent(event: AudioEvent) {
        if (event !is TrackStartEvent && manuallyStopping) {
            channel.send(Unit)
            logger.debug("AudioPlayer stop event has been received.")
        }
        if (event is TrackStartEvent) {

        } else if (event is TrackEndEvent) {
            if (!manuallyStopping) {
                val playingContent = this.playingContent
                clearState()
                listeners.forEach {
                    it.onContentEnd(playingContent!!)
                }
            }
        } else if (event is TrackStuckEvent) {
            val playingContent = this.playingContent
            clearState()
            listeners.forEach {
                it.onContentStop(playingContent!!)
            }
        } else if (event is TrackExceptionEvent) {
            val playingContent = this.playingContent
            clearState()
            listeners.forEach {
                it.onContentStop(playingContent!!)
            }
        } else {
            val playingContent = this.playingContent
            clearState()
            listeners.forEach {
                it.onContentStop(playingContent!!)
            }
        }
    }
}