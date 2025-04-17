package sidim.doma.service

import dev.inmo.tgbotapi.types.ChatId
import sidim.doma.util.LocalizationUtils.getText

class GameSubscriptionService(
    private val gameService: GameService,
    private val userGameStateService: UserGameStateService,
    private val messageService: MessageService,
    private val uiService: BotUiService,
    private val userService: UserService
) {
    suspend fun handleSubscribe(chatId: ChatId, messageText: String, locale: String) {
        if (!ensureUserRegistered(chatId, locale)) return

        val chatIdStr = chatId.chatId.toString()
        val appid = messageText.removePrefix("/subscribe_")
        val isBanned = userGameStateService.checkIsBannedByGameIdAndUserId(appid, chatIdStr)

        when (isBanned) {
            false -> messageService.sendTextMessage(
                chatId,
                getText("message.already_subscribed", locale) + appid
            )

            true -> {
                userGameStateService.updateIsBannedByGameIdAndUserId(false, appid, chatIdStr)
                messageService.sendTextMessage(chatId, getText("message.subscribe", locale) + appid)
            }

            null -> return
        }
    }

    suspend fun handleUnsubscribe(chatId: ChatId, messageText: String, locale: String) {
        if (!ensureUserRegistered(chatId, locale)) return

        val chatIdStr = chatId.chatId.toString()
        val appid = messageText.removePrefix("/unsubscribe_")
        val isBanned = userGameStateService.checkIsBannedByGameIdAndUserId(appid, chatIdStr)

        when (isBanned) {
            true -> messageService.sendTextMessage(
                chatId,
                getText("message.already_unsubscribed", locale) + appid
            )

            false -> {
                userGameStateService.updateIsBannedByGameIdAndUserId(true, appid, chatIdStr)
                messageService.sendTextMessage(
                    chatId,
                    getText("message.unsubscribe", locale) + appid
                )
            }

            null -> return
        }
    }

    suspend fun handleLinksToGame(chatId: ChatId, messageText: String, locale: String) {
        val appid = messageText.removePrefix("/links_to_game_")
        messageService.sendTextMessage(
            chatId,
            getText("message.links_to_game_message", locale, appid, appid)
        )
    }

    suspend fun handleBlackList(chatId: ChatId, messageText: String, locale: String) {
        val pageNumber = messageText.removePrefix("/black_list_").toInt()
        val pageSize = 10

        val bannedGamesPage =
            gameService.getPageBannedByChatId(chatId.chatId.toString(), pageNumber, pageSize)
        if (bannedGamesPage.items.isEmpty()) {
            messageService.sendTextMessage(chatId, getText("message.empty_black_list", locale))
        } else {
            messageService.sendMessageWithKeyboard(
                chatId,
                getText("message.black_list", locale, bannedGamesPage.totalItems),
                uiService.blackListKeyboard(
                    bannedGamesPage.items,
                    bannedGamesPage.currentPage,
                    bannedGamesPage.totalPages
                )
            )
        }
    }

    suspend fun handleClearBlackList(chatId: ChatId, locale: String) {
        val bannedGamesCount = gameService.getCountBannedByChatId(chatId.chatId.toString())
        if (bannedGamesCount == 0L) {
            messageService.sendTextMessage(chatId, getText("message.empty_black_list", locale))
        } else {
            userGameStateService.clearBlackListByUserId(chatId.chatId.toString())
            messageService.sendTextMessage(chatId, getText("message.black_list_clear", locale))
        }
    }

    private suspend fun ensureUserRegistered(chatId: ChatId, locale: String): Boolean {
        if (!userService.existsByChatId(chatId.chatId.toString())) {
            messageService.sendTextMessage(chatId, getText("message.not_registered", locale))
            return false
        }
        return true
    }
}