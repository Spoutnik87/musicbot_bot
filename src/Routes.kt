package fr.spoutnik87

import fr.spoutnik87.command.*
import fr.spoutnik87.event.WebRequestEvent
import fr.spoutnik87.reader.*
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.getOrFail
import java.util.concurrent.ConcurrentHashMap

@UseExperimental(io.ktor.util.KtorExperimentalAPI::class)
fun Routing.root() {

    val commandList = ConcurrentHashMap<String, WebCommand>()
    commandList["play"] = PlayContentCommand()
    commandList["stop"] = StopContentCommand()
    commandList["clear"] = ClearQueueCommand()
    commandList["setPosition"] = SetPositionCommand()
    commandList["pause"] = PauseCommand()
    commandList["resume"] = ResumeCommand()

    route("server") {
        /**
         * Get server status
         */
        get("/{guildId}") {
            val guildId = call.parameters.getOrFail("guildId")
            val server = BotApplication.getServer(guildId)
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                call.respond(server.getStatus())
            }
        }
        /**
         * Add a content to queue
         */
        post("/play/{guildId}") {
            val reader = call.receive<PlayContentReader>()
            val guildId = call.parameters.getOrFail("guildId")
            val server = BotApplication.getServer(guildId)
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                commandList["play"]?.execute(WebRequestEvent(reader), server)
                call.respond(server.getStatus())
            }
        }
        /**
         * Remove a content from the queue
         */
        post("/stop/{guildId}") {
            val reader = call.receive<StopContentReader>()
            val guildId = call.parameters.getOrFail("guildId")
            val server = BotApplication.getServer(guildId)
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                commandList["stop"]?.execute(WebRequestEvent(reader), server)
                call.respond(server.getStatus())
            }
        }
        /**
         * Clear queue
         */
        post("/clear/{guildId}") {
            val reader = call.receive<ClearQueueReader>()
            val guildId = call.parameters.getOrFail("guildId")
            val server = BotApplication.getServer(guildId)
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                commandList["clear"]?.execute(WebRequestEvent(reader), server)
                call.respond(server.getStatus())
            }
        }
        /**
         * Update currently playing content position.
         */
        post("/position/{guildId}") {
            val reader = call.receive<UpdateContentPositionReader>()
            val guildId = call.parameters.getOrFail("guildId")
            val server = BotApplication.getServer(guildId)
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                commandList["setPosition"]?.execute(WebRequestEvent(reader), server)
                call.respond(server.getStatus())
            }
        }

        /**
         * Pause playing content
         */
        post("/pause/{guildId}") {
            val reader = call.receive<PauseContentReader>()
            val guildId = call.parameters.getOrFail("guildId")
            val server = BotApplication.getServer(guildId)
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                commandList["pause"]?.execute(WebRequestEvent(reader), server)
                call.respond(server.getStatus())
            }
        }

        /**
         * Resume playing content
         */
        post("/resume/{guildId}") {
            val reader = call.receive<ResumeContentReader>()
            val guildId = call.parameters.getOrFail("guildId")
            val server = BotApplication.getServer(guildId)
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                commandList["resume"]?.execute(WebRequestEvent(reader), server)
                call.respond(server.getStatus())
            }
        }
    }

    get("/stop") {
        BotApplication.stop()
        call.respondText("Bot stopped!")
    }
}