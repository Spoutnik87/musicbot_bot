package fr.spoutnik87.bot

class ClearQueueCommand : WebCommand {

    override suspend fun execute(event: WebRequestEvent, server: Server) {
        server.clearContents()
    }
}