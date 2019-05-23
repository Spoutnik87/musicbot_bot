package fr.spoutnik87

import fr.spoutnik87.bot.*
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

@UseExperimental(io.ktor.util.KtorExperimentalAPI::class)
fun Routing.root() {

    val commandList = HashMap<String, WebCommand>()
    commandList["play"] = PlayContentCommand()
    commandList["stop"] = StopContentCommand()
    commandList["clear"] = ClearQueueCommand()
    commandList["setPosition"] = SetPositionCommand()
    commandList["pause"] = PauseCommand()
    commandList["unPause"] = UnPauseCommand()

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
         * Add an audiotrack to queue
         */
        post("/play/{guildId}") {
            val reader = call.receive<PlayTrackReader>()
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
         * Remove an audiotrack from the queue
         */
        post("/stop/{guildId}") {
            val reader = call.receive<StopTrackReader>()
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
         * Update currently playing track position.
         */
        post("/position/{guildId}") {
            val reader = call.receive<UpdateTrackPositionReader>()
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
            val reader = call.receive<PauseTrackReader>()
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
        post("/unpause/{guildId}") {
            val reader = call.receive<UnPauseTrackReader>()
            val guildId = call.parameters.getOrFail("guildId")
            val server = BotApplication.getServer(guildId)
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                commandList["unPause"]?.execute(WebRequestEvent(reader), server)
                call.respond(server.getStatus())
            }
        }
    }

    get("/stop") {
        BotApplication.stop()
        call.respondText("Bot stopped!")
    }
}