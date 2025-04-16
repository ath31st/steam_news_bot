package sidim.doma.service

import com.sksamuel.aedile.core.Cache
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.dto.Statistics
import sidim.doma.entity.Game
import sidim.doma.util.Localization.getText
import sidim.doma.util.UserState
import java.io.IOException

class UserInteractionService(
    private val uiService: BotUiService,
    private val userService: UserService,
    private val gameService: GameService,
    private val steamApiClient: SteamApiClient,
    private val messageService: MessageService,
    private val userGameStateService: UserGameStateService,
    private val userStateCache: Cache<Long, UserState>,
    private val statsCache: Cache<String, Statistics>
) {
    suspend fun handleUnknownCommand(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(chatId, getText("message.default_message", locale))

    suspend fun handleStart(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(
            chatId,
            getText("message.start", locale),
            replyMarkup = uiService.mainMenuKeyboard(locale)
        )

    suspend fun handleHelp(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(chatId, getText("message.help", locale))

    suspend fun handleSettings(chatId: IdChatIdentifier, locale: String) =
        messageService.sendTextMessage(
            chatId,
            getText("message.settings", locale),
            replyMarkup = uiService.mainMenuKeyboard(locale)
        )

    suspend fun handleStats(chatId: IdChatIdentifier, locale: String) {
        val (countUsers, countActiveUsers, countGames) = statsCache.get("stats", compute = {
            Statistics(
                userService.countUsers(),
                userService.countActiveUsers(),
                gameService.countGames()
            )
        })

        messageService.sendTextMessage(
            chatId,
            getText("message.stats", locale, countUsers, countActiveUsers, countGames)
        )
    }

    suspend fun handleSetSteamId(chatId: ChatId, locale: String) {
        messageService.sendTextMessage(chatId, getText("message.enter_steam_id", locale))
        userStateCache.put(chatId.chatId.long, UserState.SET_STEAM_ID)
    }

    suspend fun handleCheckSteamId(chatId: ChatId, locale: String) {
        val user = userService.getUserByChatId(chatId.chatId.long)
            ?: return sendNotRegistered(chatId, locale)

        val status = if (user.active) getText("message.active", locale)
        else getText("message.inactive", locale)
        messageService.sendTextMessage(
            chatId,
            getText("message.check_steam_id", locale, user.steamId) + status
        )
    }

    suspend fun handleSetActiveMode(chatId: ChatId, locale: String) {
        if (!ensureUserRegistered(chatId, locale)) return

        userService.updateActiveByChatId(true, chatId.chatId.toString())
        messageService.sendTextMessage(chatId, getText("message.active_mode", locale))
    }


    suspend fun handleSetInactiveMode(chatId: ChatId, locale: String) {
        if (!ensureUserRegistered(chatId, locale)) return

        userService.updateActiveByChatId(false, chatId.chatId.toString())
        messageService.sendTextMessage(chatId, getText("message.inactive_mode", locale))
    }

    suspend fun handleCheckWishlist(chatId: ChatId, locale: String) {
        val user = userService.getUserByChatId(chatId.chatId.long)
            ?: return sendNotRegistered(chatId, locale)

        val responseCode = steamApiClient.checkWishlistAvailability(user.steamId)
        val response = when (responseCode) {
            200 -> getText("message.wishlist_available", locale)
            500 -> getText("message.wishlist_not_available", locale)
            else -> getText("message.problem_with_network_or_steam_service", locale)
        }
        messageService.sendTextMessage(chatId, response)
    }


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
            messageService.sendTextMessage(chatId, getText("message.default_message", locale))
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
        messageService.sendTextMessage(chatId, getText("message.waiting", locale))
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

            userService.registerOrUpdateUser(chatIdStr, name, steamId, locale)

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
                getText("message.incorrect_steam_id", locale)
            )
            false
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
                getText("message.error_hidden_acc", locale, steamId)
            )
            null
        } catch (e: IOException) {
            messageService.sendTextMessage(
                chatId,
                getText("message.error_dont_exists_acc", locale, steamId)
            )
            null
        } catch (e: Exception) {
            messageService.sendTextMessage(chatId, getText("message.error_common", locale))
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
            getText(
                "message.registration",
                locale,
                steamId,
                name ?: getText("message.default_name", locale),
                userGameStateService.countByUserIdAndIsOwned(chatIdStr, true),
                userGameStateService.countByUserIdAndIsWished(chatIdStr, true)
            )
        )
    }

    private suspend fun getUserState(chatId: IdChatIdentifier): UserState? =
        userStateCache.getIfPresent(chatId.chatId.long)

    private fun clearUserState(chatId: IdChatIdentifier) =
        userStateCache.invalidate(chatId.chatId.long)


    private suspend fun sendNotRegistered(chatId: ChatId, locale: String) =
        messageService.sendTextMessage(chatId, getText("message.not_registered", locale))


    private suspend fun ensureUserRegistered(chatId: ChatId, locale: String): Boolean {
        if (!userService.existsByChatId(chatId.chatId.toString())) {
            sendNotRegistered(chatId, locale)
            return false
        }
        return true
    }
}