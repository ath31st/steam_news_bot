package sidim.doma.plugin

import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.chat.CommonUser
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.utils.RiskFeature
import sidim.doma.service.UserInteractionService

class BotController(
    private val interactionService: UserInteractionService
) {
    @OptIn(RiskFeature::class)
    suspend fun registerHandlers(context: BehaviourContext) {
        with(context) {
            onCommand("start") { message ->
                val chatId = message.chat.id
                val locale = getUserLocale(message)
                interactionService.handleStart(chatId, locale)
            }

            onCommand("help") { message ->
                val chatId = message.chat.id
                val locale = getUserLocale(message)
                interactionService.handleHelp(chatId, locale)
            }

            onCommand("settings") { message ->
                val chatId = message.chat.id
                val locale = getUserLocale(message)
                interactionService.handleSettings(chatId, locale)
            }

            onCommand("stats") { message ->
                val chatId = message.chat.id
                val locale = getUserLocale(message)
                interactionService.handleStats(chatId, locale)
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

                    callback.data.startsWith("/subscribe_") -> interactionService.handleSubscribe(
                        chatId,
                        callback.data,
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

                    else -> interactionService.handleUnknownCommand(chatId, locale)
                }
                bot.answerCallbackQuery(callback)
            }

            onText(initialFilter = { it.text?.startsWith("/") != true }) { message ->
                val chatId = message.chat.id
                val locale = getUserLocale(message)
                val name = message.from?.username?.username
                interactionService.handleTextInput(chatId, name, message.text ?: "", locale)
            }
        }
    }

    @OptIn(RiskFeature::class)
    private fun getUserLocale(message: CommonMessage<*>): String =
        (message.from as CommonUser).languageCode ?: "en"
}