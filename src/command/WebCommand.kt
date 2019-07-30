package fr.spoutnik87.command

import fr.spoutnik87.bot.Server
import fr.spoutnik87.event.WebRequestEvent

/**
 * Base interface of Web Commands
 */
interface WebCommand : Command {

    suspend fun execute(event: WebRequestEvent, server: Server)
}