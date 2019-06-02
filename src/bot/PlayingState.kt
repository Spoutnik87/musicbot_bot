package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class PlayingState(
    override val bot: Bot,
    override var track: AudioTrack?
) : BotState {

    override var position: Long? = null

    init {
        track?.position = position ?: 0
        bot.playTrack(track!!)
    }

    override suspend fun playTrack(track: AudioTrack?) {
        // A track is already playing
    }

    override suspend fun stopTrack() {}

    override suspend fun setPosition(position: Long) {}

    override suspend fun pauseTrack() {}

    override suspend fun resumeTrack() {}
}