package sidim.doma.service

import com.sksamuel.aedile.core.Cache
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.entity.Game
import sidim.doma.util.LocalizationUtils.getText
import sidim.doma.util.UserState
import java.io.IOException

class UserManagementService(
    private val userService: UserService,
    private val gameService: GameService,
    private val steamApiClient: SteamApiClient,
    private val messageService: MessageService,
    private val userGameStateService: UserGameStateService,
    private val userStateCache: Cache<Long, UserState>
) {
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

    suspend fun handleTextInput(chatId: IdChatIdentifier, name: String, text: String, locale: String) {
        val state = getUserState(chatId)
        if (state == UserState.SET_STEAM_ID) {
            handleSteamIdInput(chatId, name, text, locale)
        } else {
            messageService.sendTextMessage(chatId, getText("message.default_message", locale))
        }
    }

    private suspend fun handleSteamIdInput(chatId: IdChatIdentifier, name: String, steamId: String, locale: String) {
        if (!validateAndPrepare(chatId, steamId, locale)) return
        val gameLists = getGamesFromSteam(chatId, steamId, locale) ?: return
        saveUserAndGamesTransactional(chatId, name, steamId.toLong(), locale, gameLists)
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

    private fun saveUserAndGamesTransactional(
        chatId: IdChatIdentifier,
        name: String,
        steamId: Long,
        locale: String,
        gameLists: Pair<List<Game>, List<Game>>
    ) = transaction {
        val (ownedGames, wishedGames) = gameLists
        val allGames = (ownedGames + wishedGames).distinctBy { it.appid }

        val chatIdStr = chatId.chatId.toString()
        val user = userService.getUserByChatId(chatIdStr)

        userService.registerOrUpdateUser(chatIdStr, name, steamId, locale)

        if (user == null || user.steamId != steamId) {
            userGameStateService.deleteUgsByUserId(chatIdStr)
            gameService.createGames(allGames)
            userGameStateService.createUgsByUserId(chatIdStr, ownedGames, wishedGames)
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
        } catch (_: NullPointerException) {
            messageService.sendTextMessage(
                chatId,
                getText("message.error_hidden_acc", locale, steamId)
            )
            null
        } catch (_: IOException) {
            messageService.sendTextMessage(
                chatId,
                getText("message.error_dont_exists_acc", locale, steamId)
            )
            null
        } catch (_: Exception) {
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