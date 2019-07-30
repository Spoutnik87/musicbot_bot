package fr.spoutnik87.command

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Configuration
import fr.spoutnik87.bot.Server
import org.slf4j.LoggerFactory

class HelpCommand(
    override val prefix: String
) : TextCommand {

    private val logger = LoggerFactory.getLogger(HelpCommand::class.java)

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        val channel = messageEvent.message.channel.block() ?: return
        logger.debug("A command has been received on server ${server.guild.id.asString()}")
        channel.createMessage(
            """
            ----------------------------- I am Musicbot -----------------------------
            -> Je peux être piloté via une interface Web accessible sur : https://musicbot.spout.cc
            -> Mais je peux aussi être piloté comme tous les autres bots via des commandes que voici :
            -> ${Configuration.superPrefix}playlist : Affiche la playlist.
            -> ${Configuration.superPrefix}play <lien> [position] : Joue le contenu (seul YouTube est supporté actuellement) Il est possible de le démarrer à une position personnalisée (en secondes).
            -> ${Configuration.superPrefix}replay : Rejoue le contenu
            -> ${Configuration.superPrefix}force <lien> [position] : Remplace le contenu joué actuellement. Il est possible de le démarrer à une position personnalisée (en secondes).
            -> ${Configuration.superPrefix}stop : Stop le bot et vide la playlist.
            -> ${Configuration.superPrefix}skip : Arrete le morceau en cours.
            -> ${Configuration.superPrefix}pause : Met en pause le morceau en cours.
            -> ${Configuration.superPrefix}resume : Reprend le morceau en cours.
            -> ${Configuration.superPrefix}setpos <position> : Joue le morceau à partir du temps indiqué. (en secondes)
            -> ${Configuration.superPrefix}dev : Affiche des informations pour les développeurs.
            
            -> J'ai été conçu par @Spout#1308 et je suis open source. 
            -> Si vous avez des suggestions ou souhaitez contribuer, n'hesitez pas !
            -------------------------------------------------------------------------
            """.trimIndent()
        ).block()
    }
}