package fr.spoutnik87.bot

class UnPauseCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        server.unPauseContent()
    }
}