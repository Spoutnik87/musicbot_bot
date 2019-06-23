package fr.spoutnik87.bot

import discord4j.core.`object`.entity.Guild
import fr.spoutnik87.RestClient
import fr.spoutnik87.model.ContentViewModel
import fr.spoutnik87.model.QueueViewModel
import fr.spoutnik87.model.RestServerModel
import fr.spoutnik87.viewmodel.ServerViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Server(
    val guild: Guild,
    val player: ContentPlayer
) : QueueListener, ContentPlayerListener {

    private var server: RestServerModel? = null
    var initialized = false
    var linkable = false

    val bot = Bot(this)
    val queue = Queue(this)

    init {
        player.addListener(this)
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
        if (!player.isPlaying()) {
            if (bot.canJoin(content.initiator)) {
                if (bot.joinVoiceChannel(content.initiator)) {
                    player.play(content)
                }
            }
        } else {
            queue.addContent(content)
        }
    }

    suspend fun stopPlayingContent() {
        player.stop()
    }

    fun playNextContent() {
        val content = queue.next()
        if (content != null) {
            player.play(content)
        } else {
            bot.leaveVoiceChannel()
        }
    }

    suspend fun clearContents() {
        stopPlayingContent()
        queue.clear()
        bot.leaveVoiceChannel()
    }

    fun setContentPosition(position: Long) {
        player.setPosition(position)
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
            .map { ContentViewModel(it.uid, it.id, it.initiator, null, null, null) }), player.getState().let {
            if (it.content != null) {
                ContentViewModel(
                    it.content.uid,
                    it.content.id,
                    it.content.initiator,
                    it.startTime,
                    it.position ?: 0,
                    it.paused
                )
            } else {
                null
            }
        })
    }

    fun pauseContent() {
        player.pause()
    }

    fun resumeContent() {
        player.resume()
    }

    override suspend fun onAddContent(content: Content) {}

    override suspend fun onRemoveContent(content: Content) {}

    override suspend fun onClear() {}

    override fun onContentStart(content: Content) {}

    override fun onContentStop(content: Content) {}

    override fun onContentEnd(content: Content) {
        playNextContent()
    }

    override fun onContentStartFailure(content: Content) {}

    override fun onContentPause(content: Content, position: Long) {}

    override fun onContentResume(content: Content, position: Long) {}

    override fun onContentPositionChange(content: Content, oldPosition: Long, newPosition: Long) {}
}