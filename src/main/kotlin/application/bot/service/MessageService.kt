package sidim.doma.application.bot.service

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.exceptions.CommonRequestException
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sidim.doma.domain.user.service.UserService

class MessageService(
    private val bot: TelegramBot,
    private val userService: UserService,
    private val uiService: BotUiService
) {
    private val logger: Logger = LoggerFactory.getLogger("MessageService")

    suspend fun sendTextMessage(
        chatId: IdChatIdentifier,
        text: String,
        replyMarkup: InlineKeyboardMarkup? = null
    ) {
        try {
            bot.sendTextMessage(
                chatId,
                text,
                parseMode = HTMLParseMode,
                replyMarkup = replyMarkup,
                disableNotification = true
            )
        } catch (e: CommonRequestException) {
            if (e.response.errorCode == 403) {
                val chatIdStr = chatId.chatId.toString()
                userService.updateActiveByChatId(false, chatIdStr).let {
                    when (it) {
                        1 -> logger.info("User $chatIdStr deactivated")
                        0 -> logger.info("Failed to deactivate user $chatIdStr")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error while sending message to $chatId: ${e.message}")
        }
    }

    suspend fun sendNewsMessage(
        chatId: ChatId,
        text: String,
        appid: String,
        locale: String,
    ) {
        sendTextMessage(chatId, text, uiService.newsMenuKeyboard(appid, locale))
    }

    suspend fun sendMessageWithKeyboard(
        chatId: ChatId,
        text: String,
        keyboard: InlineKeyboardMarkup
    ) {
        sendTextMessage(chatId, text, keyboard)
    }
}