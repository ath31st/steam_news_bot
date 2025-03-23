package sidim.doma.plugin

import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.chat.CommonUser
import dev.inmo.tgbotapi.utils.RiskFeature
import sidim.doma.service.BotUiService
import sidim.doma.service.MessageService
import sidim.doma.service.UserInteractionService

class BotController(
    private val uiService: BotUiService,
    private val messageService: MessageService,
    private val interactionService: UserInteractionService
) {
    @OptIn(RiskFeature::class)
    suspend fun registerHandlers(context: BehaviourContext) {
        with(context) {
            onCommand("start") { message ->
                val chatId = message.chat.id
                val locale = (message.from as CommonUser).languageCode ?: "en"
                messageService.sendTextMessage(
                    chatId,
                    Localization.getText("message.start", locale),
                    replyMarkup = uiService.mainMenuKeyboard(locale)
                )
            }

            onCommand("help") { message ->
                val chatId = message.chat.id
                val locale = (message.from as CommonUser).languageCode ?: "en"
                messageService.sendTextMessage(chatId, Localization.getText("message.help", locale))
            }

            onCommand("settings") { message ->
                val chatId = message.chat.id
                val locale = (message.from as CommonUser).languageCode ?: "en"
                messageService.sendTextMessage(
                    chatId,
                    Localization.getText("message.settings", locale),
                    replyMarkup = uiService.mainMenuKeyboard(locale)
                )
            }

            onDataCallbackQuery { callback ->
                val chatId = callback.from.id
                val locale = callback.user.languageCode ?: "en"
                when {
                    callback.data == "/set_steam_id" -> interactionService.handleSetSteamId(
                        chatId,
                        locale
                    )

                    callback.data == "/check_steam_id" -> interactionService.handleCheckSteamId(
                        chatId,
                        locale
                    )

                    callback.data == "/set_active_mode" -> interactionService.handleSetActiveMode(
                        chatId,
                        locale
                    )

                    callback.data == "/set_inactive_mode" -> interactionService.handleSetInactiveMode(
                        chatId,
                        locale
                    )

                    callback.data == "/check_wishlist" -> interactionService.handleCheckWishlist(
                        chatId,
                        locale
                    )

                    callback.data.startsWith("/unsubscribe") -> interactionService.handleUnsubscribe(
                        chatId,
                        callback.data,
                        locale
                    )

                    callback.data.startsWith("/links_to_game") -> interactionService.handleLinksToGame(
                        chatId,
                        callback.data,
                        locale
                    )

                    callback.data == "/black_list" -> interactionService.handleBlackList(
                        chatId,
                        locale
                    )

                    callback.data == "/clear_black_list" -> interactionService.handleClearBlackList(
                        chatId,
                        locale
                    )

                    else -> {
                        messageService.sendTextMessage(
                            chatId,
                            Localization.getText("message.default_message", locale)
                        )
                    }
                }
                bot.answerCallbackQuery(callback)
            }

            onText(initialFilter = { it.text?.startsWith("/") != true }) { message ->
                val chatId = message.chat.id
                val user = message.from as CommonUser
                val locale = user.languageCode ?: "en"
                val name = user.username?.username
                interactionService.handleTextInput(chatId, name, message.text ?: "", locale)
            }
        }
    }
}