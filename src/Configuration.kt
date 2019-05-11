package fr.spoutnik87

import io.ktor.application.Application

class Configuration(
    val application: Application
) {

    val token
        get() = application.environment.config.property("ktor.token").getString()

    val filesPath
        get() = application.environment.config.property("ktor.filesPath").getString()

    val username
        get() = application.environment.config.property("ktor.username").getString()

    val password
        get() = application.environment.config.property("ktor.password").getString()

    val apiUrl
        get() = application.environment.config.property("ktor.apiUrl").getString()
}