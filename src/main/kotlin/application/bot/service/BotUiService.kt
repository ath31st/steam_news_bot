package sidim.doma.application.bot.service

import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardRowBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.KeyboardButtonStyle
import dev.inmo.tgbotapi.utils.row
import sidim.doma.common.util.LocalizationUtils
import sidim.doma.domain.game.entity.Game

class BotUiService {
    fun startMenuKeyboard(locale: String): InlineKeyboardMarkup = inlineKeyboard {
        row {
            menuButton(
                emoji = "🔑",
                labelKey = "button.set_upd_steam_id",
                locale = locale,
                data = "/set_steam_id",
                style = KeyboardButtonStyle.Primary
            )
        }
        row {
            menuButton(
                emoji = "❓",
                labelKey = "button.help_steam_id",
                locale = locale,
                data = "/help",
                style = KeyboardButtonStyle.Primary
            )
        }
    }

    fun mainMenuKeyboard(locale: String): InlineKeyboardMarkup = inlineKeyboard {
        row {
            menuButton(
                emoji = "🔑",
                labelKey = "button.set_upd_steam_id",
                locale = locale,
                data = "/set_steam_id",
                style = KeyboardButtonStyle.Primary
            )
            menuButton(
                emoji = "🆔",
                labelKey = "button.check_steam_id",
                locale = locale,
                data = "/check_steam_id"
            )
        }
        row {
            menuButton(
                emoji = "✅",
                labelKey = "button.set_active_mode",
                locale = locale,
                data = "/set_active_mode",
                style = KeyboardButtonStyle.Success
            )
            menuButton(
                emoji = "💤",
                labelKey = "button.set_inactive_mode",
                locale = locale,
                data = "/set_inactive_mode"
            )
        }
        row {
            menuButton(
                emoji = "🧹",
                labelKey = "button.clear_black_list",
                locale = locale,
                data = "/clear_black_list",
                style = KeyboardButtonStyle.Danger
            )
            menuButton(
                emoji = "📋",
                labelKey = "button.black_list",
                locale = locale,
                data = "/black_list_1",
                style = KeyboardButtonStyle.Primary
            )
        }
        row {
            menuButton(
                emoji = "💜",
                labelKey = "button.check_wishlist",
                locale = locale,
                data = "/check_wishlist",
                style = KeyboardButtonStyle.Success
            )
        }
    }

    fun newsMenuKeyboard(appid: String, locale: String): InlineKeyboardMarkup =
        inlineKeyboard {
            row {
                menuButton(
                    emoji = "🚫",
                    labelKey = "button.unsubscribe",
                    locale = locale,
                    data = "/unsubscribe_$appid",
                    style = KeyboardButtonStyle.Danger
                )
                menuButton(
                    emoji = "🔗",
                    labelKey = "button.links_to_game",
                    locale = locale,
                    data = "/links_to_game_$appid",
                    style = KeyboardButtonStyle.Primary
                )
            }
        }

    fun blackListKeyboard(
        banList: List<Game>,
        currentPage: Int,
        totalPages: Int
    ): InlineKeyboardMarkup = inlineKeyboard {
        banList.forEach { game ->
            row {
                dataButton(
                    text = "➕ ${game.name ?: game.appid}",
                    data = "/subscribe_${game.appid}",
                    style = KeyboardButtonStyle.Success
                )
            }
        }
        if (totalPages > 1) {
            row {
                if (currentPage > 1) {
                    dataButton(
                        text = "⬅️ ${currentPage - 1}",
                        data = "/black_list_${currentPage - 1}",
                        style = KeyboardButtonStyle.Primary
                    )
                }
                dataButton(
                    text = "📄 $currentPage/$totalPages",
                    data = "/black_list_$currentPage"
                )
                if (currentPage < totalPages) {
                    dataButton(
                        text = "${currentPage + 1} ➡️",
                        data = "/black_list_${currentPage + 1}",
                        style = KeyboardButtonStyle.Primary
                    )
                }
            }
        }
    }

    private fun InlineKeyboardRowBuilder.menuButton(
        emoji: String,
        labelKey: String,
        locale: String,
        data: String,
        style: KeyboardButtonStyle? = null
    ) {
        dataButton(
            text = "$emoji ${LocalizationUtils.getButton(labelKey, locale)}",
            data = data,
            style = style
        )
    }
}
