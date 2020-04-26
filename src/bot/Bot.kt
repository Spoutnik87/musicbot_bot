package fr.spoutnik87.bot

import discord4j.core.`object`.VoiceState
import discord4j.rest.util.Permission
import discord4j.rest.util.Snowflake
import discord4j.voice.VoiceConnection
import fr.spoutnik87.BotApplication
import kotlinx.coroutines.reactive.awaitFirst
import java.util.concurrent.ConcurrentHashMap

class Bot(
    private val server: Server
) {

    private var voiceConnection: VoiceConnection? = null
    var currentChannelId: String? = null

    var voiceStates = ConcurrentHashMap<Snowflake, VoiceState>()

    suspend fun canJoin(userId: String) = canJoin(Snowflake.of(userId))

    suspend fun canJoin(userId: Snowflake): Boolean {
        val voiceState = voiceStates[userId] ?: return false
        if (!voiceState.channelId.isPresent) {
            return false
        }
        val channel = voiceState.channel.awaitFirst() ?: return false
        val member =
            BotApplication.discordClient.getMemberById(server.guild.id, userId).data.awaitFirst() ?: return false
        val permissions =
            channel.getEffectivePermissions(Snowflake.of(member.user().id())).awaitFirst()?.asEnumSet() ?: return false
        return permissions.contains(Permission.CONNECT) && permissions.contains(Permission.SPEAK)
    }

    suspend fun joinVoiceChannel(userId: Snowflake): Boolean {
        if (currentChannelId != null) {
            leaveVoiceChannel()
        }
        val voiceState = voiceStates[userId] ?: return false
        val channel = voiceState.channel.awaitFirst() ?: return false
        voiceConnection = channel.join {
            it.setProvider(server.player.provider)
        }.awaitFirst() ?: return false
        currentChannelId = channel.id.asString()
        return true
    }

    suspend fun joinVoiceChannel(userId: String) = joinVoiceChannel(Snowflake.of(userId))

    fun leaveVoiceChannel() {
        if (voiceConnection != null) {
            voiceConnection?.disconnect()
            voiceConnection = null
        }
        currentChannelId = null
    }
}
