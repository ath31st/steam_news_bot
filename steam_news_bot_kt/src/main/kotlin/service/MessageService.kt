package sidim.doma.service

import dev.inmo.tgbotapi.bot.TelegramBot

import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.HTMLParseMode

class MessageService(
    private val bot: TelegramBot,
    private val userService: UserService,
    private val uiService: BotUiService
) {
    suspend fun sendTextMessage(
        chatId: IdChatIdentifier,
        text: String,
        replyMarkup: InlineKeyboardMarkup? = null
    ) {
        try {
            bot.sendTextMessage(chatId, text, parseMode = HTMLParseMode, replyMarkup = replyMarkup)
        } catch (e: Exception) {
            if (e.message?.contains("403") == true) {
                userService.updateActiveByChatId(false, chatId.toString())
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