package sidim.doma.application.bot.service

import dev.inmo.tgbotapi.types.ChatId
import sidim.doma.common.util.LocalizationUtils
import sidim.doma.domain.user.service.UserService
import sidim.doma.infrastructure.integration.steam.SteamApiClient

class WishlistService(
    private val steamApiClient: SteamApiClient,
    private val userService: UserService,
    private val messageService: MessageService
) {
    suspend fun handleCheckWishlist(chatId: ChatId, locale: String) {
        val user = userService.getUserByChatId(chatId.chatId.long)
            ?: return messageService.sendTextMessage(
                chatId,
                LocalizationUtils.getText("message.not_registered", locale)
            )

        val responseCode = steamApiClient.checkWishlistAvailability(user.steamId)
        val response = when (responseCode) {
            200 -> LocalizationUtils.getText("message.wishlist_available", locale)
            500 -> LocalizationUtils.getText("message.wishlist_not_available", locale)
            else -> LocalizationUtils.getText(
                "message.problem_with_network_or_steam_service",
                locale
            )
        }
        messageService.sendTextMessage(chatId, response)
    }
}