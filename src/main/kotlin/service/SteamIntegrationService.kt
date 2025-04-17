package sidim.doma.service

import dev.inmo.tgbotapi.types.ChatId
import sidim.doma.util.LocalizationUtils.getText

class SteamIntegrationService(
    private val steamApiClient: SteamApiClient,
    private val userService: UserService,
    private val messageService: MessageService
) {
    suspend fun handleCheckWishlist(chatId: ChatId, locale: String) {
        val user = userService.getUserByChatId(chatId.chatId.long)
            ?: return messageService.sendTextMessage(
                chatId,
                getText("message.not_registered", locale)
            )

        val responseCode = steamApiClient.checkWishlistAvailability(user.steamId)
        val response = when (responseCode) {
            200 -> getText("message.wishlist_available", locale)
            500 -> getText("message.wishlist_not_available", locale)
            else -> getText("message.problem_with_network_or_steam_service", locale)
        }
        messageService.sendTextMessage(chatId, response)
    }
}