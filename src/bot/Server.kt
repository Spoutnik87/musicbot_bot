package fr.spoutnik87.bot

import discord4j.core.`object`.entity.Guild
import fr.spoutnik87.BotApplication
import fr.spoutnik87.Configuration
import fr.spoutnik87.RestClient
import fr.spoutnik87.feature.Feature
import fr.spoutnik87.model.RestServerModel
import fr.spoutnik87.viewmodel.ContentViewModel
import fr.spoutnik87.viewmodel.QueueViewModel
import fr.spoutnik87.viewmodel.ServerViewModel
import kotlinx.coroutines.CompletableDeferred
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.primaryConstructor

class Server(
    val guild: Guild,
    val player: ContentPlayer
) : QueueListener, ContentPlayerListener {

    private val logger = LoggerFactory.getLogger(Server::class.java)

    private var server: RestServerModel? = null
    var initialized = false
    var linkable = false

    val bot = Bot(this)
    val queue = Queue(this)

    private val features = ConcurrentHashMap<String, Feature>()

    suspend fun setFeature(name: String, enable: Boolean) {
        val feature = BotApplication.featureList[name] ?: return
        if (enable && features.none { it.value::class == feature }) {
            features[name] = feature.primaryConstructor?.call(this)!!
            features[name]?.start()
        } else if (!enable && features.any { it.value::class == feature }) {
            features[name]?.stop()
            features.remove(name)
        }
    }

    suspend fun init() {
        player.init()
        player.addListener(this)
        loadServerData()
    }

    /**
     * Load initial data.
     */
    suspend fun loadServerData() {
        if (Configuration.restApi) {
            val server = RestClient.getServerByGuildId(guild.id.asString())
            if (server == null) {
                linkable = true
                initialized = false
            } else {
                this.server = server
                linkable = !server.linked
                initialized = true
            }
        } else {
            initialized = true
            linkable = false
        }
    }

    suspend fun playContent(content: Content) {
        val response = CompletableDeferred<ContentPlayerState>()
        player.send(GetState(response))
        val state = response.await()
        if (!state.playing) {
            if (bot.joinVoiceChannel(content.initiator)) {
                player.send(Play(content))
            }
        } else {
            queue.addContent(content)
        }
    }

    suspend fun stopPlayingContent() {
        player.send(Stop)
    }

    suspend fun playNextContent() {
        val content = queue.next()
        if (content != null) {
            player.send(Play(content))
        } else {
            bot.leaveVoiceChannel()
        }
    }

    suspend fun clearContents() {
        queue.clear()
        player.send(Stop)
        bot.leaveVoiceChannel()
    }

    suspend fun setContentPosition(position: Long) {
        player.send(SetPosition(position))
    }

    /**
     * Force the specified content to be played.
     * Replace the currently playing content if the bot is already playing something.
     * @param content Content to be played
     */
    suspend fun replacePlayingContent(content: Content) {
        val response = CompletableDeferred<ContentPlayerState>()
        player.send(GetState(response))
        val state = response.await()
        if (!state.playing) {
            if (bot.joinVoiceChannel(content.initiator)) {
                player.send(Play(content))
            }
        } else {
            player.send(Play(content))
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
            logger.debug("Guild with id ${guild.id.asString()} is successfully linked with server ${server.id}")
            return server
        } catch (e: Exception) {
            logger.error("An error happened during an attempt to link Guild ${guild.id.asString()} to a server.")
        }
        return null
    }

    suspend fun getStatus(): ServerViewModel {
        val response = CompletableDeferred<ContentPlayerState>()
        player.send(GetState(response))
        val state = response.await()
        return ServerViewModel(
            Date().time, guild.id.asString(), QueueViewModel(
                queue.getAllContents()
            .map { ContentViewModel(it.uid, it.id, it.initiator, null, null, null, it.name, it.duration) }),
            state.let {
            if (it.content != null) {
                ContentViewModel(
                    it.content.uid,
                    it.content.id,
                    it.content.initiator,
                    it.startTime,
                    it.position ?: 0,
                    it.paused,
                    it.content.name,
                    it.content.duration
                )
            } else {
                null
            }
        })
    }

    suspend fun pauseContent() {
        player.send(Pause)
    }

    suspend fun resumeContent() {
        player.send(Resume)
    }

    override suspend fun onAddContent(content: Content) {}

    override suspend fun onRemoveContent(content: Content) {}

    override suspend fun onClear() {}

    override fun onContentStart(content: Content) {}

    override fun onContentStop(content: Content) {}

    override suspend fun onContentEnd(content: Content) {
        playNextContent()
    }

    override fun onContentStartFailure(content: Content) {}

    override fun onContentPause(content: Content, position: Long) {}

    override fun onContentResume(content: Content, position: Long) {}

    override fun onContentPositionChange(content: Content, oldPosition: Long, newPosition: Long) {}
}
