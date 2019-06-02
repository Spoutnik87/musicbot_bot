package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class PausedState(
    override val bot: Bot
) : BotState {

    override var track: AudioTrack? = null
    override var position: Long? = null

    override suspend fun playTrack(track: AudioTrack?) {
        bot.state = PlayingState(bot, track ?: this.track)
    }

    override suspend fun stopTrack() {}

    override suspend fun setPosition(position: Long) {}

    override suspend fun pauseTrack() {}

    override suspend fun resumeTrack() {}
}