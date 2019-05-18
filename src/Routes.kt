package fr.spoutnik87

import fr.spoutnik87.reader.ClearTrackReader
import fr.spoutnik87.reader.PlayTrackReader
import fr.spoutnik87.reader.StopTrackReader
import fr.spoutnik87.reader.UpdateTrackPositionReader
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.root(discordBot: DiscordBot) {

    route("server") {
        /**
         * Get server status
         */
        get("/{guildId}") {
            val guildId = call.parameters["guildId"]
            val server = discordBot.serverList[guildId]
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
            val guildId = call.parameters["guildId"]
            val server = discordBot.serverList[guildId]
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                server.addTrack(reader.id, reader.initiator)
                call.respond(server.getStatus())
            }
        }
        /**
         * Remove an audiotrack from the queue
         */
        post("/stop/{guildId}") {
            val reader = call.receive<StopTrackReader>()
            val guildId = call.parameters["guildId"]
            val server = discordBot.serverList[guildId]
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                server.removeTrack(reader.id, reader.initiator)
                call.respond(server.getStatus())
            }
        }
        /**
         * Clear queue
         */
        post("/clear/{guildId}") {
            val reader = call.receive<ClearTrackReader>()
            val guildId = call.parameters["guildId"]
            val server = discordBot.serverList[guildId]
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                server.clearTracks(reader.initiator)
                call.respond(server.getStatus())
            }
        }
        /**
         * Update currently playing track position.
         */
        post("/position/{guildId}") {
            val reader = call.receive<UpdateTrackPositionReader>()
            val guildId = call.parameters["guildId"]
            val server = discordBot.serverList[guildId]
            if (server == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                server.updateTrackPosition(reader.id, reader.initiator, reader.position)
                call.respond(server.getStatus())
            }
        }
    }

    get("/stop") {
        discordBot.stop()
        call.respondText("Bot stopped!")
    }
}