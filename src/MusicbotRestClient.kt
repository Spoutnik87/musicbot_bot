package fr.spoutnik87

import com.beust.klaxon.Klaxon
import fr.spoutnik87.model.MusicbotRestLoginModel
import fr.spoutnik87.model.MusicbotRestServerJoinModel
import fr.spoutnik87.model.MusicbotRestServerLinkModel
import fr.spoutnik87.model.MusicbotRestServerModel
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.call
import io.ktor.client.request.header
import io.ktor.client.response.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking

class MusicbotRestClient(
    val discordBot: DiscordBot
) {

    private var token: String? = null

    init {
        runBlocking {
            login()
        }
    }

    private suspend fun login() {
        val call = doRequestWithBody(
            "${discordBot.configuration.apiUrl}/login",
            HttpMethod.Post,
            Klaxon().toJsonString(
                MusicbotRestLoginModel(
                    discordBot.configuration.username,
                    discordBot.configuration.password
                )
            ),
            false
        )
        if (call.response.status.value == HttpStatusCode.OK.value) {
            token = call.response.headers["Authorization"]?.substring("Bearer ".length)
        }
    }

    suspend fun getServerByGuildId(guildId: String): MusicbotRestServerModel? {
        return makeRequest<MusicbotRestServerModel>(
            "${discordBot.configuration.apiUrl}/server/guild/$guildId",
            HttpMethod.Get
        )
    }

    suspend fun linkGuildToServer(userId: String, guildId: String, token: String): MusicbotRestServerModel? {
        return makeRequestWithBody(
            "${discordBot.configuration.apiUrl}/server/link",
            HttpMethod.Post,
            Klaxon().toJsonString(MusicbotRestServerLinkModel(userId, guildId, token))
        )
    }

    suspend fun joinServer(userId: String, guildId: String, token: String): Any? {
        return makeRequestWithBody(
            "${discordBot.configuration.apiUrl}/user/joinServer",
            HttpMethod.Post,
            Klaxon().toJsonString(MusicbotRestServerJoinModel(userId, guildId, token))
        )
    }

    private suspend inline fun <reified T> makeRequest(url: String, method: HttpMethod, withToken: Boolean = true): T? {
        return makeRequestWithBody<T>(url, method, null, withToken)
    }

    private suspend inline fun <reified T> makeRequestWithBody(
        url: String,
        method: HttpMethod,
        body: Any?,
        withToken: Boolean = true
    ): T? {
        if (token == null) {
            login()
        }

        var call = doRequestWithBody(url, method, body, withToken)
        if (call.response.status.value == HttpStatusCode.Forbidden.value) {
            login()
            call = doRequestWithBody(url, method, body, withToken)
        }
        if (call.response.status.value == HttpStatusCode.OK.value || call.response.status.value == HttpStatusCode.Accepted.value) {
            return Klaxon().parse<T>(call.response.readText(Charsets.UTF_8))
        } else {
            return null
        }
    }

    private suspend fun doRequest(url: String, method: HttpMethod, withToken: Boolean = true): HttpClientCall {
        return doRequestWithBody(url, method, null, withToken)
    }

    private suspend fun doRequestWithBody(
        url: String,
        method: HttpMethod,
        body: Any?,
        withToken: Boolean = true
    ): HttpClientCall {
        val client = HttpClient()
        val call = client.call(url) {
            this.method = method
            if (withToken) header("Authorization", "Bearer $token")
            if (body != null && body is String) this.body =
                TextContent(body, contentType = ContentType.Application.Json)
        }
        return call
    }

    fun logout() {
        token = null
    }
}