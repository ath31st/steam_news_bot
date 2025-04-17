package sidim.doma.application.bot.controller

import dev.inmo.tgbotapi.types.ChatId
import org.slf4j.LoggerFactory
import sidim.doma.application.bot.command.*
import sidim.doma.application.bot.service.UserInteraction

class CallbackCommandRegistry(
    private val interaction: UserInteraction
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val commands: Map<String, CallbackCommand> = mapOf(
        "/set_steam_id" to SetSteamIdCommand(interaction),
        "/check_steam_id" to CheckSteamIdCommand(interaction),
        "/set_active_mode" to SetActiveModeCommand(interaction),
        "/set_inactive_mode" to SetInactiveModeCommand(interaction),
        "/check_wishlist" to CheckWishlistCommand(interaction),
        "/clear_black_list" to ClearBlackListCommand(interaction)
    )

    private val prefixCommands: Map<String, CallbackCommand> = mapOf(
        "/subscribe" to SubscribeCommand(interaction),
        "/unsubscribe" to UnsubscribeCommand(interaction),
        "/links_to_game" to LinksToGameCommand(interaction),
        "/black_list" to BlackListCommand(interaction)
    )

    suspend fun executeCommand(chatId: ChatId, data: String, locale: String) {
        logger.info("Executing callback command: $data for chatId: $chatId")
        val command = commands[data] ?: prefixCommands.entries
            .find { data.startsWith(it.key) }
            ?.value ?: UnknownCommand(interaction)
        command.execute(chatId, data, locale)
    }
}