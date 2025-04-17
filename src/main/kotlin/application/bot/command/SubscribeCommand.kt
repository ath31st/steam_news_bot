package sidim.doma.application.bot.command

import dev.inmo.tgbotapi.types.ChatId
import sidim.doma.application.bot.service.UserInteraction

class SubscribeCommand(private val interaction: UserInteraction) : CallbackCommand {
    override suspend fun execute(chatId: ChatId, data: String, locale: String) {
        interaction.handleSubscribe(chatId, data, locale)
    }
}