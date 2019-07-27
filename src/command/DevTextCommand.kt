package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.bot.Server
import fr.spoutnik87.bot.TextCommand

class DevTextCommand(override val prefix: String) : TextCommand {
    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        val channel = messageEvent.message.channel.block() ?: return

        channel.createMessage(
            """
            ----------------------------- I am Musicbot -----------------------------
            -> Mes dépôts sont accessible sur Github aux adresses suivantes :
            -> https://github.com/spoutnik87/musicbot_bot : Module communiquant avec l'API Discord
            -> https://github.com/spoutnik87/musicbot_web : L'interface Web pouvant me piloter
            -> https://github.com/spoutnik87/musicbot_rest : L'API REST servant d'interface entre les deux autres modules.
            
            -> Si vous avez des suggestions ou souhaitez contribuer, n'hesitez pas !
            -------------------------------------------------------------------------
            """.trimIndent()
        ).block()
    }
}