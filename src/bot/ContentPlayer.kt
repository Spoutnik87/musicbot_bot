package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import fr.spoutnik87.BotApplication
import fr.spoutnik87.Configuration
import java.util.concurrent.Semaphore

class ContentPlayer(
    private val audioPlayer: AudioPlayer
) : AudioLoadResultHandler {

    val provider = LavaPlayerAudioProvider(audioPlayer)

    private var playingContent: Content? = null

    private var audioTrack: AudioTrack? = null

    private var playingAudioTrack: AudioTrack? = null

    private var startTime: Long? = null

    private val lock = Object()

    private val listeners: ArrayList<ContentPlayerListener> = ArrayList()

    private var paused = false

    private var pausing = false

    private var updatingPosition = false

    private var manuallyStopping = false

    private val loadingTrackSemaphore = Semaphore(0)

    private val trackEventSemaphore = Semaphore(0)

    init {
        audioPlayer.addListener {
            onTrackEvent(it)
        }
    }

    fun play(content: Content) {
        synchronized(lock) {
            stop()
            this.playingContent = content
            if (content.link != null) {
                BotApplication.playerManager.loadItem(content.link, this)
            } else {
                BotApplication.playerManager.loadItem(Configuration.filesPath + "media/" + content.id, this)
            }
            loadingTrackSemaphore.acquire()
            if (playingAudioTrack != null) {
                startTime = System.currentTimeMillis()
                playingAudioTrack?.position = playingContent?.position ?: 0
                audioPlayer.playTrack(playingAudioTrack)
                listeners.forEach {
                    it.onContentStart(content)
                }
            } else {
                clearState()
                listeners.forEach {
                    it.onContentStartFailure(content)
                }
            }
        }
    }

    fun set(content: Content) {
        synchronized(lock) {
            if (isPlaying()) {
                manuallyStopping = true
                stop()
                trackEventSemaphore.acquire()
                manuallyStopping = false
            }
            play(content)
        }
    }

    /**
     * Stop the playing content.
     */
    fun stop() {
        synchronized(lock) {
            if (isPlaying()) {
                audioPlayer.stopTrack()
            }
        }
    }

    fun blockingStop() {
        synchronized(lock) {
            if (isPlaying()) {
                manuallyStopping = true
                audioPlayer.stopTrack()
                trackEventSemaphore.acquire()
                manuallyStopping = false
            }
        }
    }

    /**
     * Pause the currently playing content if it's in a playing state.
     */
    fun pause() {
        synchronized(lock) {
            if (isPlaying()) {
                pausing = true
                paused = true
                manuallyStopping = true
                audioPlayer.stopTrack()
                trackEventSemaphore.acquire()
                manuallyStopping = false
                pausing = false
                listeners.forEach {
                    it.onContentPause(playingContent!!, playingAudioTrack?.position!!)
                }
            }
        }
    }

    /**
     * Resume the playing content if it's in a paused state.
     */
    fun resume() {
        synchronized(lock) {
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
            }
        }
    }

    /**
     * Return true if the playing content is paused.
     */
    fun isPaused(): Boolean {
        synchronized(lock) {
            return paused
        }
    }

    /**
     * Set a new position
     */
    fun setPosition(position: Long) {
        synchronized(lock) {
            if (isPlaying()) {
                var oldPosition: Long?
                if (isPaused()) {
                    oldPosition = playingAudioTrack?.position
                    playingAudioTrack?.position = position
                } else {
                    updatingPosition = true
                    manuallyStopping = true
                    audioPlayer.stopTrack()
                    trackEventSemaphore.acquire()
                    manuallyStopping = false
                    oldPosition = playingAudioTrack?.position
                    playingAudioTrack = audioTrack?.makeClone()
                    playingAudioTrack?.position = position
                    audioPlayer.playTrack(playingAudioTrack)
                    updatingPosition = false
                }
                listeners.forEach {
                    it.onContentPositionChange(playingContent!!, oldPosition!!, position)
                }
            }
        }
    }

    /**
     * Get the current content position or null if there is no content playing.
     */
    fun getPosition(): Long? {
        synchronized(lock) {
            return playingAudioTrack?.position
        }
    }

    /**
     * Return the playing content or null if there is no content playing.
     */
    fun getPlayingContent(): Content? {
        synchronized(lock) {
            return playingContent
        }
    }

    /**
     * Return the start time of the playing content in millis.
     * Return null if there is no content playing.
     */
    fun getStartTime(): Long? {
        synchronized(lock) {
            return startTime
        }
    }

    /**
     * Return true if a content is loaded and playing or paused.
     */
    fun isPlaying(): Boolean {
        synchronized(lock) {
            return playingContent != null
        }
    }

    /**
     * Return the current state of the player.
     */
    fun getState(): ContentPlayerState {
        synchronized(lock) {
            return ContentPlayerState(playingContent, isPlaying(), isPaused(), getStartTime(), getPosition())
        }
    }

    fun addListener(listener: ContentPlayerListener) {
        synchronized(lock) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: ContentPlayerListener) {
        synchronized(lock) {
            listeners.remove(listener)
        }
    }

    private fun clearState() {
        paused = false
        pausing = false
        updatingPosition = false
        playingContent = null
        audioTrack = null
        playingAudioTrack = null
        startTime = null
    }

    private fun onTrackEvent(event: AudioEvent) {
        synchronized(lock) {
            if (event !is TrackStartEvent && manuallyStopping) {
                trackEventSemaphore.release()
            }
        }
        if (event is TrackStartEvent) {

        } else if (event is TrackEndEvent) {
            if (!manuallyStopping) {
                synchronized(lock) {
                    val playingContent = this.playingContent
                    clearState()
                    listeners.forEach {
                        it.onContentEnd(playingContent!!)
                    }
                }
            }
        } else if (event is TrackStuckEvent) {
            synchronized(lock) {
                val playingContent = this.playingContent
                clearState()
                listeners.forEach {
                    it.onContentStop(playingContent!!)
                }
            }
        } else if (event is TrackExceptionEvent) {
            synchronized(lock) {
                val playingContent = this.playingContent
                clearState()
                listeners.forEach {
                    it.onContentStop(playingContent!!)
                }
            }
        } else {
            synchronized(lock) {
                val playingContent = this.playingContent
                clearState()
                listeners.forEach {
                    it.onContentStop(playingContent!!)
                }
            }
        }
    }

    override fun trackLoaded(track: AudioTrack?) {
        this.audioTrack = track?.makeClone()
        this.playingAudioTrack = track
        loadingTrackSemaphore.release()
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        loadingTrackSemaphore.release()
    }

    override fun noMatches() {
        loadingTrackSemaphore.release()
    }

    override fun loadFailed(exception: FriendlyException) {
        loadingTrackSemaphore.release()
    }
}