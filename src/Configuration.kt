package fr.spoutnik87

import io.ktor.application.Application

object Configuration {

    private lateinit var application: Application

    operator fun invoke(application: Application) {
        this.application = application
    }

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