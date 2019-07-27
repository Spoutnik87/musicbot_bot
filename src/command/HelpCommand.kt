package fr.spoutnik87.bot

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.spoutnik87.Configuration

class HelpCommand(
    override val prefix: String
) : TextCommand {

    override suspend fun execute(messageEvent: MessageCreateEvent, server: Server) {
        val channel = messageEvent.message.channel.block() ?: return

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
            
            -> J'ai été conçu par @Spout#1308 et je suis open source.
            -> Mes dépôts sont accessible sur Github aux adresses :
            -> https://github.com/spoutnik87/musicbot_bot : Module communiquant avec l'API Discord
            -> https://github.com/spoutnik87/musicbot_web : L'interface Web pouvant me piloter
            -> https://github.com/spoutnik87/musicbot_rest : L'API REST servant d'interface entre les deux autres modules.
            
            -> Si vous avez des suggestions ou souhaitez contribuer, n'hesitez pas !
            -------------------------------------------------------------------------
            """.trimIndent()
        ).block()
    }
}