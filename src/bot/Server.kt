package fr.spoutnik87.bot

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import discord4j.core.`object`.entity.Guild
import fr.spoutnik87.BotApplication
import fr.spoutnik87.Configuration
import fr.spoutnik87.RestClient
import fr.spoutnik87.model.ContentViewModel
import fr.spoutnik87.model.QueueViewModel
import fr.spoutnik87.model.RestServerModel
import fr.spoutnik87.viewmodel.ServerViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Server(
    val guild: Guild,
    val player: AudioPlayer
) : QueueListener, ContentListener {

    private var server: RestServerModel? = null
    var initialized = false
    var linkable = false

    var playingContent: Content? = null
        private set

    val bot = Bot(this)
    val queue = Queue(this)

    init {
        GlobalScope.launch {
            loadServerData()
        }
    }

    /**
     * Load initial data.
     */
    suspend fun loadServerData() {
        val server = RestClient.getServerByGuildId(guild.id.asString())
        if (server == null) {
            linkable = true
            initialized = false
        } else {
            this.server = server
            linkable = !server.linked
            initialized = true
        }
    }

    suspend fun playContent(content: Content) {
        if (playingContent == null) {
            playingContent = content
            val scheduler = TrackScheduler(this, content)
            BotApplication.playerManager.loadItem(Configuration.filesPath + "media\\" + content.id, scheduler)
            content.startTime = System.currentTimeMillis()
        } else {
            queue.addContent(content)
        }
    }

    suspend fun stopPlayingContent() {
        if (playingContent == null) {
            return
        }
        bot.stopTrackWithoutEvent()
        playingContent = null
    }

    suspend fun playNextContent() {
        val content = queue.next()
        if (content != null) {
            playContent(content)
        } else {
            bot.leaveVoiceChannel()
        }
    }

    suspend fun clearContents() {
        stopPlayingContent()
        queue.clearQueue()
        bot.leaveVoiceChannel()
    }

    fun setContentPosition(position: Long) {
        if (playingContent != null) {
            bot.setTrackPosition(position)
        }
    }

    suspend fun linkServer(linkToken: String, userId: String): RestServerModel? {
        try {
            val server = RestClient.linkGuildToServer(
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

    fun getStatus(): ServerViewModel {
        return ServerViewModel(guild.id.asString(), QueueViewModel(queue.getAllContents()
            .map { ContentViewModel(it.uid, it.id, it.initiator, null, null, null) }), playingContent.let {
            if (it != null) {
                ContentViewModel(it.uid, it.id, it.initiator, it.startTime, it.position ?: 0, bot.isPaused)
            } else {
                null
            }
        })
    }

    fun pauseContent() {
        bot.pausePlayingTrack()
    }

    fun unPauseContent() {
        bot.unPausePlayingTrack()
        playingContent?.loadTrack(bot.playingTrack)
    }

    override suspend fun onAddContent(content: Content) {}

    override suspend fun onRemoveContent(content: Content) {}

    override suspend fun onClearQueue() {}

    override suspend fun onContentStart() {
        playingContent?.startTime = System.currentTimeMillis()
    }

    override suspend fun onContentStop() {
        playingContent = null
        playNextContent()
    }

    /**
     * Called when a content is ready to play.
     */
    override suspend fun onContentLoad(content: Content) {
        if (bot.currentChannelId == null) {
            if (!bot.joinVoiceChannel(content.initiator)) {
                playingContent = null
            }
        }
        val track = content.audioTrack
        if (track != null) {
            bot.playTrack(track)
        }
    }
}