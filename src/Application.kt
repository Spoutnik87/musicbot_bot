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

/*fun main() {

    GlobalScope.launch {
        async {
            Mono.just("oui").delayElement(Duration.ofMillis(2000)).doOnNext { println(it) }.block()
        }
        async {
            Mono.just("oui2").delayElement(Duration.ofMillis(2000)).doOnNext { println(it) }.block()
        }
    }

    runBlocking {
        println("Reached")
        delay(6000)
    }
}*/