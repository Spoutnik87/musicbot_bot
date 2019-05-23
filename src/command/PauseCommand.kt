package fr.spoutnik87.bot

class PauseCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        server.pauseContent()
    }
}