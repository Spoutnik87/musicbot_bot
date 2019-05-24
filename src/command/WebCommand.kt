package fr.spoutnik87.bot

import fr.spoutnik87.Command

/**
 * Base interface of Web Commands
 */
interface WebCommand : Command {

    suspend fun execute(event: WebRequestEvent, server: Server)
}