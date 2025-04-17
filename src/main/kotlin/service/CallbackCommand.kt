package sidim.doma.service

import dev.inmo.tgbotapi.types.ChatId
import org.slf4j.LoggerFactory

interface CallbackCommand {
    suspend fun execute(chatId: ChatId, data: String, locale: String)
}

class SetSteamIdCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleSetSteamId(chatId, locale)
    }
}

class CheckSteamIdCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleCheckSteamId(chatId, locale)
    }
}

class SetActiveModeCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleSetActiveMode(chatId, locale)
    }
}

class SetInactiveModeCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleSetInactiveMode(chatId, locale)
    }
}

class CheckWishlistCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleCheckWishlist(chatId, locale)
    }
}

class SubscribeCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleSubscribe(chatId, data, locale)
    }
}

class UnsubscribeCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleUnsubscribe(chatId, data, locale)
    }
}

class LinksToGameCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleLinksToGame(chatId, data, locale)
    }
}

class BlackListCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleBlackList(chatId, data, locale)
    }
}

class ClearBlackListCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleClearBlackList(chatId, locale)
    }
}

class UnknownCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleUnknownCommand(chatId, locale)
    }
}

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