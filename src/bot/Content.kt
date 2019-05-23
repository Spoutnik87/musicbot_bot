package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class Content(
    /**
     * Used to identify this content in queue.
     */
    val uid: String,
    /**
     * Content UUID
     */
    val id: String,
    /**
     * The user who added this Content
     * User UUID
     */
    val initiator: String
) {

    var loaded = false
        private set

    var audioTrack: AudioTrack? = null
        private set

    val duration: Long?
        get() = audioTrack?.duration

    val position: Long?
        get() = audioTrack?.position

    var startTime: Long? = null

    fun loadTrack(track: AudioTrack) {
        audioTrack = track
        loaded = true
    }

    fun unloadTrack(track: AudioTrack) {
        audioTrack = null
        loaded = false
    }
}