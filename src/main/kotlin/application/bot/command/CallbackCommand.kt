package sidim.doma.application.bot.command

import dev.inmo.tgbotapi.types.ChatId

interface CallbackCommand {
    suspend fun execute(chatId: ChatId, data: String, locale: String)
}