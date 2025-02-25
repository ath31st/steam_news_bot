package sidim.doma.service

import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sidim.doma.plugin.Localization
import sidim.doma.util.UserState
import java.io.IOException

class UserInteractionService(
    private val userService: UserService,
    private val gameService: GameService,
    private val steamApiClient: SteamApiClient,
    private val messageService: MessageService,
    private val userGameStateService: UserGameStateService
) {
    private val maxSize = 50
    private val userStates = linkedMapOf<Long, UserState>()
    private val mutex = Mutex()

    suspend fun handleSetSteamId(chatId: ChatId, locale: String) {
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.enter_steam_id", locale)
        )
        mutex.withLock {
            if (userStates.size >= maxSize) {
                userStates.remove(userStates.keys.first())
            }
            userStates[chatId.chatId.long] = UserState.SET_STEAM_ID
        }
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

    suspend fun handleTextInput(chatId: IdChatIdentifier, text: String, locale: String) {
        val state = getUserState(chatId)
        if (state == UserState.SET_STEAM_ID) {
            handleSteamIdInput(chatId, text, locale)
        } else {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.default_message", locale)
            )
        }
    }

    private suspend fun handleSteamIdInput(
        chatId: IdChatIdentifier,
        steamId: String,
        locale: String
    ) {
        if (!isValidSteamId(steamId, chatId, locale)) {
            clearUserState(chatId)
            return
        }

        messageService.sendTextMessage(chatId, Localization.getText("message.waiting", locale))
        registerOrUpdateUser(chatId, steamId, locale)
        clearUserState(chatId)
    }

    private suspend fun isValidSteamId(
        steamId: String,
        chatId: IdChatIdentifier,
        locale: String
    ): Boolean {
        return if (steamApiClient.isValidSteamId(steamId)) {
            true
        } else {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.incorrect_steam_id", locale)
            )
            false
        }
    }

    @OptIn(PreviewFeature::class)
    private suspend fun registerOrUpdateUser(
        chatId: IdChatIdentifier,
        steamId: String,
        locale: String
    ) {
        val chatIdStr = chatId.chatId.toString()
        val name = chatId.fromUserOrNull()?.user?.firstName
        try {
            if (userService.existsByChatId(chatIdStr)) {
                userService.updateUser(chatIdStr, name, steamId, locale)
            } else {
                userService.createUser(chatIdStr, name, steamId, locale)
            }
            sendRegistrationSuccessMessage(chatId, steamId, name, chatIdStr, locale)
        } catch (e: NullPointerException) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.error_hidden_acc", locale, steamId)
            )
        } catch (e: IOException) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.error_dont_exists_acc", locale, steamId)
            )
        }
    }

    private suspend fun sendRegistrationSuccessMessage(
        chatId: IdChatIdentifier,
        steamId: String,
        name: String?,
        chatIdStr: String,
        locale: String
    ) {
        messageService.sendTextMessage(
            chatId,
            Localization.getText(
                "message.registration",
                locale,
                steamId,
                name ?: Localization.getText("message.default_name", locale),
                userGameStateService.countByUserIdAndIsOwned(chatIdStr, true),
                userGameStateService.countByUserIdAndIsWished(chatIdStr, true)
            )
        )
    }

    private suspend fun getUserState(chatId: IdChatIdentifier): UserState? {
        return mutex.withLock { userStates[chatId.chatId.long] }
    }

    private suspend fun clearUserState(chatId: IdChatIdentifier) {
        mutex.withLock { userStates.remove(chatId.chatId.long) }
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