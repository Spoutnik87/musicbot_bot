package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TrackScheduler(
    val server: Server,
    val id: String,
    val initiator: String
) : AudioLoadResultHandler {

    override fun trackLoaded(track: AudioTrack) {
        /**
         * Add content to the queue and trigger an event.
         */
        server.queue.addContent(Content(id, initiator, track))
        GlobalScope.launch {
            server.onTrackLoad()
        }
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {}

    override fun noMatches() {}

    override fun loadFailed(exception: FriendlyException) {}
}