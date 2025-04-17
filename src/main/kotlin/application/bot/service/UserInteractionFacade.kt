package sidim.doma.application.bot.service

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import sidim.doma.application.statistics.service.StatisticsService
import sidim.doma.common.util.LocalizationUtils

class UserInteractionFacade(
    private val userManagementService: UserManagementService,
    private val gameSubscriptionService: GameSubscriptionService,
    private val wishlistService: WishlistService,
    private val statisticsService: StatisticsService,
    private val messageService: MessageService,
    private val uiService: BotUiService
) : UserInteraction {
    override suspend fun handleUnknownCommand(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(
            chatId,
            LocalizationUtils.getText("message.default_message", locale)
        )

    override suspend fun handleStart(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(chatId, LocalizationUtils.getText("message.start", locale))

    override suspend fun handleHelp(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(chatId, LocalizationUtils.getText("message.help", locale))

    override suspend fun handleSettings(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(
            chatId,
            LocalizationUtils.getText("message.settings", locale),
            replyMarkup = uiService.mainMenuKeyboard(locale)
        )

    override suspend fun handleStats(chatId: IdChatIdentifier, locale: String) =
        statisticsService.handleStats(chatId, locale)

    override suspend fun handleSetSteamId(chatId: ChatId, locale: String) =
        userManagementService.handleSetSteamId(chatId, locale)

    override suspend fun handleCheckSteamId(chatId: ChatId, locale: String) =
        userManagementService.handleCheckSteamId(chatId, locale)

    override suspend fun handleSetActiveMode(chatId: ChatId, locale: String) =
        userManagementService.handleSetActiveMode(chatId, locale)

    override suspend fun handleSetInactiveMode(chatId: ChatId, locale: String) =
        userManagementService.handleSetInactiveMode(chatId, locale)

    override suspend fun handleCheckWishlist(chatId: ChatId, locale: String) =
        wishlistService.handleCheckWishlist(chatId, locale)

    override suspend fun handleSubscribe(chatId: ChatId, messageText: String, locale: String) =
        gameSubscriptionService.handleSubscribe(chatId, messageText, locale)

    override suspend fun handleUnsubscribe(chatId: ChatId, messageText: String, locale: String) =
        gameSubscriptionService.handleUnsubscribe(chatId, messageText, locale)

    override suspend fun handleLinksToGame(chatId: ChatId, messageText: String, locale: String) =
        gameSubscriptionService.handleLinksToGame(chatId, messageText, locale)

    override suspend fun handleBlackList(chatId: ChatId, messageText: String, locale: String) =
        gameSubscriptionService.handleBlackList(chatId, messageText, locale)

    override suspend fun handleClearBlackList(chatId: ChatId, locale: String) =
        gameSubscriptionService.handleClearBlackList(chatId, locale)

    override suspend fun handleTextInput(
        chatId: IdChatIdentifier,
        name: String,
        text: String,
        locale: String
    ) =
        userManagementService.handleTextInput(chatId, name, text, locale)
}