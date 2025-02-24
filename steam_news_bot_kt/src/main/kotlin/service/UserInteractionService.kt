package sidim.doma.service

import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.utils.PreviewFeature
import sidim.doma.plugin.Localization
import java.io.IOException

class UserInteractionService(
    private val userService: UserService,
    private val gameService: GameService,
    private val steamApiClient: SteamApiClient,
    private val messageService: MessageService,
    private val userGameStateService: UserGameStateService
) {
    private val userStates = mutableMapOf<Long, String>()

    suspend fun handleSetSteamId(chatId: ChatId, locale: String) {
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.enter_steam_id", locale)
        )
        userStates[chatId.chatId.long] = "SET_STEAM_ID"
    }

    suspend fun handleCheckSteamId(chatId: ChatId, locale: String) {
        val user = userService.getUserByChatId(chatId.chatId.long)
            ?: return sendNotRegistered(chatId, locale)

        val status = if (user.active) Localization.getText("message.active", locale)
        else Localization.getText("message.inactive", locale)
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.check_steam_id", locale, user.steamId) + status
        )
    }

    suspend fun handleSetActiveMode(chatId: ChatId, locale: String) {
        if (!ensureUserRegistered(chatId, locale)) return

        userService.updateActiveByChatId(true, chatId.chatId.toString())
        messageService.sendTextMessage(chatId, Localization.getText("message.active_mode", locale))
    }


    suspend fun handleSetInactiveMode(chatId: ChatId, locale: String) {
        if (!ensureUserRegistered(chatId, locale)) return

        userService.updateActiveByChatId(false, chatId.chatId.toString())
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.inactive_mode", locale)
        )
    }

    suspend fun handleCheckWishlist(chatId: ChatId, locale: String) {
        val user = userService.getUserByChatId(chatId.chatId.long)
            ?: return sendNotRegistered(chatId, locale)

        val responseCode = steamApiClient.checkWishlistAvailability(user.steamId)
        val response = when (responseCode) {
            200 -> Localization.getText("message.wishlist_available", locale)
            500 -> Localization.getText("message.wishlist_not_available", locale)
            else -> Localization.getText("message.problem_with_network_or_steam_service", locale)
        }
        messageService.sendTextMessage(chatId, response)
    }

    suspend fun handleUnsubscribe(chatId: ChatId, messageText: String, locale: String) {
        if (!ensureUserRegistered(chatId, locale)) return

        val gameTitle = messageText.substringBefore(System.lineSeparator())
        if (userGameStateService.checkIsBannedByGameNameAndUserId(
                gameTitle,
                chatId.chatId.toString()
            )
        ) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.already_unsubscribed", locale) + gameTitle
            )
        } else {
            userGameStateService.updateIsBannedByGameNameAndUserId(
                isBanned = true,
                gameTitle,
                chatId.chatId.toString(),
            )
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.unsubscribe", locale) + gameTitle
            )
        }
    }

    suspend fun handleLinksToGame(chatId: ChatId, messageText: String, locale: String) {
        val gameAppId = messageText.substringAfter("LINK(").substringBefore(")")
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.links_to_game_message", locale, gameAppId, gameAppId)
        )
    }

    suspend fun handleBlackList(chatId: ChatId, locale: String) {
        val banList = gameService.getBanListByChatId(chatId.chatId.toString())
        if (banList.isBlank()) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.empty_black_list", locale)
            )
        } else {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.black_list", locale) + banList
            )
        }
    }

    suspend fun handleClearBlackList(chatId: ChatId, locale: String) {
        val banList = gameService.getBanListByChatId(chatId.chatId.toString())
        if (banList.isBlank()) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.empty_black_list", locale)
            )
        } else {
            userGameStateService.clearBlackListByUserId(chatId.chatId.toString())
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.black_list_clear", locale)
            )
        }
    }

    @OptIn(PreviewFeature::class)
    suspend fun handleTextInput(chatId: IdChatIdentifier, text: String, locale: String) {
        val state = userStates[chatId.chatId.long]
        if (state == "SET_STEAM_ID") {
            if (!steamApiClient.isValidSteamId(text)) {
                messageService.sendTextMessage(
                    chatId,
                    Localization.getText("message.incorrect_steam_id", locale)
                )
                return
            }

            messageService.sendTextMessage(chatId, Localization.getText("message.waiting", locale))

            try {
                val chatIdStr = chatId.chatId.toString()
                val name = chatId.fromUserOrNull()?.user?.firstName
                if (userService.existsByChatId(chatIdStr)) {
                    userService.updateUser(chatIdStr, name, text, locale)
                } else {
                    userService.createUser(
                        chatIdStr,
                        name,
                        text,
                        locale
                    )
                }
                messageService.sendTextMessage(
                    chatId,
                    Localization.getText(
                        "message.registration",
                        locale,
                        text,
                        name ?: Localization.getText("message.default_name", locale),
                        userGameStateService.countByUserIdAndIsOwned(chatIdStr, true),
                        userGameStateService.countByUserIdAndIsWished(chatIdStr, true)
                    )
                )
            } catch (e: NullPointerException) {
                messageService.sendTextMessage(
                    chatId,
                    Localization.getText("message.error_hidden_acc", locale, text)
                )
            } catch (e: IOException) {
                messageService.sendTextMessage(
                    chatId,
                    Localization.getText("message.error_dont_exists_acc", locale, text)
                )
            } finally {
                userStates.remove(chatId.chatId.long)
            }
        } else {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.default_message", locale)
            )
        }
    }

    private suspend fun sendNotRegistered(chatId: ChatId, locale: String) {
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.not_registered", locale)
        )
    }

    private suspend fun ensureUserRegistered(chatId: ChatId, locale: String): Boolean {
        if (!userService.existsByChatId(chatId.chatId.toString())) {
            sendNotRegistered(chatId, locale)
            return false
        }
        return true
    }
}