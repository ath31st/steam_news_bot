package sidim.doma.application.bot.service

import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.row
import sidim.doma.common.util.LocalizationUtils
import sidim.doma.domain.game.entity.Game

class BotUiService {
    fun mainMenuKeyboard(locale: String): InlineKeyboardMarkup = inlineKeyboard {
        row {
            dataButton(
                text = "λ ${LocalizationUtils.getButton("button.set_upd_steam_id", locale)}",
                data = "/set_steam_id"
            )
            dataButton(
                text = "Ω ${LocalizationUtils.getButton("button.check_steam_id", locale)}",
                data = "/check_steam_id"
            )
        }
        row {
            dataButton(
                text = "✅ ${LocalizationUtils.getButton("button.set_active_mode", locale)}",
                data = "/set_active_mode"
            )
            dataButton(
                text = "☑ ${LocalizationUtils.getButton("button.set_inactive_mode", locale)}",
                data = "/set_inactive_mode"
            )
        }
        row {
            dataButton(
                text = "🧹 ${LocalizationUtils.getButton("button.clear_black_list", locale)}",
                data = "/clear_black_list"
            )
            dataButton(
                text = "🗑 ${LocalizationUtils.getButton("button.black_list", locale)}",
                data = "/black_list_1"
            )
        }
        row {
            dataButton(
                text = "♥ ${LocalizationUtils.getButton("button.check_wishlist", locale)}",
                data = "/check_wishlist"
            )
        }
    }

    fun newsMenuKeyboard(appid: String, locale: String): InlineKeyboardMarkup =
        inlineKeyboard {
            row {
                dataButton(
                    text = "🚫 ${LocalizationUtils.getButton("button.unsubscribe", locale)}",
                    data = "/unsubscribe_$appid"
                )
                dataButton(
                    text = "🔗 ${LocalizationUtils.getButton("button.links_to_game", locale)}",
                    data = "/links_to_game_$appid"
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
                    text = game.name ?: game.appid,
                    data = "/subscribe_${game.appid}"
                )
            }
        }
        if (totalPages > 1) {
            row {
                if (currentPage > 1) {
                    dataButton(
                        text = "⬅️ ${currentPage - 1}",
                        data = "/black_list_${currentPage - 1}"
                    )
                }
                dataButton(
                    text = "$currentPage/$totalPages",
                    data = "/black_list_$currentPage"
                )
                if (currentPage < totalPages) {
                    dataButton(
                        text = "${currentPage + 1} ➡️",
                        data = "/black_list_${currentPage + 1}"
                    )
                }
            }
        }
    }
}