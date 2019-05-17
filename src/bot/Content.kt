package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class Content(
    /**
     * Content UUID
     */
    val id: String,
    /**
     * The user who added this Content
     * User UUID
     */
    val initiator: String,
    var audioTrack: AudioTrack
) {
    val duration: Long
        get() = audioTrack.duration

    var startTime: Long? = null
}