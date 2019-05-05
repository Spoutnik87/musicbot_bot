package fr.spoutnik87

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.root(discordBot: DiscordBot) {
    get("/stop") {
        discordBot.stop()
        call.respondText("Hello World!")
    }
}