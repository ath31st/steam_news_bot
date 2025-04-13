package sidim.doma.service

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.entity.Game
import sidim.doma.plugin.Localization
import sidim.doma.util.UserState
import java.io.IOException

class UserInteractionService(
    private val uiService: BotUiService,
    private val userService: UserService,
    private val gameService: GameService,
    private val steamApiClient: SteamApiClient,
    private val messageService: MessageService,
    private val userGameStateService: UserGameStateService
) {
    private val maxSize = 50
    private val userStates = linkedMapOf<Long, UserState>()
    private val mutex = Mutex()

    suspend fun handleUnknownCommand(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.default_message", locale)
        )

    suspend fun handleStart(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.start", locale),
            replyMarkup = uiService.mainMenuKeyboard(locale)
        )

    suspend fun handleHelp(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(chatId, Localization.getText("message.help", locale))

    suspend fun handleSettings(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.settings", locale),
            replyMarkup = uiService.mainMenuKeyboard(locale)
        )

    suspend fun handleStats(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.stats", locale)
        )

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

        val appid = messageText.removePrefix("/unsubscribe_")
        if (userGameStateService.checkIsBannedByGameIdAndUserId(
                appid,
                chatId.chatId.toString()
            )
        ) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.already_unsubscribed", locale) + appid
            )
        } else {
            userGameStateService.updateIsBannedByGameIdAndUserId(
                isBanned = true,
                appid,
                chatId.chatId.toString(),
            )
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.unsubscribe", locale) + appid
            )
        }
    }

    suspend fun handleLinksToGame(chatId: ChatId, messageText: String, locale: String) {
        val appid = messageText.removePrefix("/links_to_game_")
        messageService.sendTextMessage(
            chatId,
            Localization.getText("message.links_to_game_message", locale, appid, appid)
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

    suspend fun handleTextInput(
        chatId: IdChatIdentifier,
        name: String?,
        text: String,
        locale: String
    ) {
        val state = getUserState(chatId)
        if (state == UserState.SET_STEAM_ID) {
            handleSteamIdInput(chatId, name, text, locale)
        } else {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.default_message", locale)
            )
        }
    }

    private suspend fun handleSteamIdInput(
        chatId: IdChatIdentifier,
        name: String?,
        steamId: String,
        locale: String
    ) {
        if (!validateAndPrepare(chatId, steamId, locale)) return
        val gameLists = getGamesFromSteam(chatId, steamId, locale) ?: return
        saveUserAndGames(chatId, name, steamId, locale, gameLists)
        finalizeRegistration(chatId, steamId, name, locale)
    }

    private suspend fun validateAndPrepare(
        chatId: IdChatIdentifier,
        steamId: String,
        locale: String
    ): Boolean {
        if (!isValidSteamId(steamId, chatId, locale)) {
            clearUserState(chatId)
            return false
        }
        messageService.sendTextMessage(chatId, Localization.getText("message.waiting", locale))
        return true
    }

    private fun saveUserAndGames(
        chatId: IdChatIdentifier,
        name: String?,
        steamId: String,
        locale: String,
        gameLists: Pair<List<Game>, List<Game>>
    ) {
        val (ownedGames, wishedGames) = gameLists
        val allGames = (ownedGames + wishedGames).distinctBy { it.appid }

        transaction {
            val chatIdStr = chatId.chatId.toString()
            val user = userService.getUserByChatId(chatIdStr)

            registerOrUpdateUser(chatId, name, steamId, locale)

            if (user == null || user.steamId != steamId.toLong()) {
                userGameStateService.deleteUgsByUserId(chatIdStr)
                gameService.createGames(allGames)
                userGameStateService.createUgsByUserId(chatIdStr, ownedGames, wishedGames)
            }
        }
    }

    private suspend fun finalizeRegistration(
        chatId: IdChatIdentifier,
        steamId: String,
        name: String?,
        locale: String
    ) {
        sendRegistrationSuccessMessage(chatId, steamId, name, locale)
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

    private fun registerOrUpdateUser(
        chatId: IdChatIdentifier,
        name: String?,
        steamId: String,
        locale: String
    ) {
        val chatIdStr = chatId.chatId.toString()

        if (userService.existsByChatId(chatIdStr)) {
            userService.updateUser(chatIdStr, name, steamId, locale)
        } else {
            userService.createUser(chatIdStr, name, steamId, locale)
        }
    }

    private suspend fun getGamesFromSteam(
        chatId: IdChatIdentifier,
        steamId: String,
        locale: String
    ): Pair<List<Game>, List<Game>>? {
        return try {
            val games = steamApiClient.getOwnedGames(steamId)
            val wishedGames = steamApiClient.getWishlistGames(steamId)
            Pair(games, wishedGames)
        } catch (e: NullPointerException) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.error_hidden_acc", locale, steamId)
            )
            null
        } catch (e: IOException) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.error_dont_exists_acc", locale, steamId)
            )
            null
        } catch (e: Exception) {
            messageService.sendTextMessage(
                chatId,
                Localization.getText("message.error_common", locale)
            )
            null
        }
    }

    private suspend fun sendRegistrationSuccessMessage(
        chatId: IdChatIdentifier,
        steamId: String,
        name: String?,
        locale: String
    ) {
        val chatIdStr = chatId.chatId.toString()
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