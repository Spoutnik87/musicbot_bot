package fr.spoutnik87

import io.ktor.application.Application
import io.ktor.routing.routing

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    val configuration = Configuration(this)
    val discordBot = DiscordBot(configuration)
    discordBot.start()

    routing {
        root(discordBot)
    }
}

