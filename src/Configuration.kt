package fr.spoutnik87

import io.ktor.application.Application

@OptIn(io.ktor.util.KtorExperimentalAPI::class)
object Configuration {

    private lateinit var application: Application

    operator fun invoke(application: Application) {
        this.application = application
    }

    val environment
        get() = application.environment.config.property("ktor.environment").getString()

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

    val restApi
        get() = application.environment.config.property("ktor.restApi").getString().toLowerCase() == "TRUE"

    val isDev
        get() = environment == "dev"

    val isProd
        get() = environment != "dev"

    val resources_path
        get() = if (isDev) "resources/" else ""

    val superPrefix = "!!"
}
