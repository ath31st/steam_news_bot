package sidim.doma.service

import dev.inmo.tgbotapi.bot.TelegramBot

import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
        } catch (e: Exception) {
            if (e.message?.contains("403") == true) {
                userService.updateActiveByChatId(false, chatId.toString())
                logger.info("User $chatId deactivated")
            }
        }
    }

    suspend fun sendNewsMessage(
        chatId: ChatId,
        text: String,
        appid: String,
        locale: String,
    ) {
        sendTextMessage(chatId, text, uiService.subscribeMenuKeyboard(appid, locale))
    }
}