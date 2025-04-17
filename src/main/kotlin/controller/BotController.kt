package sidim.doma.controller

import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.utils.RiskFeature
import org.slf4j.LoggerFactory
import sidim.doma.service.CallbackCommandRegistry
import sidim.doma.service.CommandHandler
import sidim.doma.service.UserInteraction
import sidim.doma.util.LocalizationUtils.getText
import sidim.doma.util.LocalizationUtils.getUserLocale

class BotController(
    private val interaction: UserInteraction
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val commandHandler = CommandHandler(interaction)
    private val callbackCommandRegistry = CallbackCommandRegistry(interaction)

    @OptIn(RiskFeature::class)
    suspend fun registerHandlers(context: BehaviourContext) {
        logger.info("Registering bot handlers")
        commandHandler.registerCommands(context)

        with(context) {
            onDataCallbackQuery { callback ->
                val chatId = callback.from.id
                val locale = callback.user.languageCode ?: "en"
                logger.info("Handling callback query: ${callback.data} for chatId: $chatId")
                callbackCommandRegistry.executeCommand(chatId, callback.data, locale)
                bot.answerCallbackQuery(callback)
            }

            onText(initialFilter = { it.text?.startsWith("/") != true }) { message ->
                val chatId = message.chat.id
                val locale = getUserLocale(message)
                val name = message.from?.username?.username ?: getText("users.default_name", locale)
                logger.info("Handling text input for chatId: $chatId, text: ${message.text}")
                interaction.handleTextInput(chatId, name, message.text ?: "", locale)
            }
        }
    }
}