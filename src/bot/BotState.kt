package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

interface BotState {

    val bot: Bot
    var track: AudioTrack?
    var position: Long?

    suspend fun playTrack(track: AudioTrack? = null)
    suspend fun stopTrack()
    suspend fun setPosition(position: Long)
    suspend fun pauseTrack()
    suspend fun resumeTrack()

    val isPaused
        get() = this is PausedState

    val isPlaying
        get() = this is PlayingState

    val isExited
        get() = this is ExitedState
}