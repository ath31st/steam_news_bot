package sidim.doma.application.bot.service

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier

interface UserInteraction {
    suspend fun handleUnknownCommand(chatId: IdChatIdentifier, locale: String)
    suspend fun handleStart(chatId: IdChatIdentifier, locale: String)
    suspend fun handleHelp(chatId: IdChatIdentifier, locale: String)
    suspend fun handleSettings(chatId: IdChatIdentifier, locale: String)
    suspend fun handleStats(chatId: IdChatIdentifier, locale: String)
    suspend fun handleSetSteamId(chatId: ChatId, locale: String)
    suspend fun handleCheckSteamId(chatId: ChatId, locale: String)
    suspend fun handleSetActiveMode(chatId: ChatId, locale: String)
    suspend fun handleSetInactiveMode(chatId: ChatId, locale: String)
    suspend fun handleCheckWishlist(chatId: ChatId, locale: String)
    suspend fun handleSubscribe(chatId: ChatId, messageText: String, locale: String)
    suspend fun handleUnsubscribe(chatId: ChatId, messageText: String, locale: String)
    suspend fun handleLinksToGame(chatId: ChatId, messageText: String, locale: String)
    suspend fun handleBlackList(chatId: ChatId, messageText: String, locale: String)
    suspend fun handleClearBlackList(chatId: ChatId, locale: String)
    suspend fun handleTextInput(
        chatId: IdChatIdentifier,
        name: String,
        text: String,
        locale: String
    )
}