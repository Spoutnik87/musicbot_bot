package fr.spoutnik87.bot

class ResumeCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        server.resumeContent()
    }
}