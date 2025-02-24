package sidim.doma.service

import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard

import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.row
import sidim.doma.plugin.Localization

class BotUiService {
    fun mainMenuKeyboard(locale: String): InlineKeyboardMarkup = inlineKeyboard {
        row {
            dataButton(
                text = "λ ${Localization.getButton("set_upd_steam_id", locale)}",
                data = "/set_steam_id"
            )
            dataButton(
                text = "Ω ${Localization.getButton("check_steam_id", locale)}",
                data = "/check_steam_id"
            )
        }
        row {
            dataButton(
                text = "✅ ${Localization.getButton("set_active_mode", locale)}",
                data = "/set_active_mode"
            )
            dataButton(
                text = "☑ ${Localization.getButton("set_inactive_mode", locale)}",
                data = "/set_inactive_mode"
            )
        }
        row {
            dataButton(
                text = "🧹 ${Localization.getButton("clear_black_list", locale)}",
                data = "/clear_black_list"
            )
            dataButton(
                text = "🗑 ${Localization.getButton("black_list", locale)}",
                data = "/black_list"
            )
        }
        row {
            dataButton(
                text = "♥ ${Localization.getButton("check_wishlist", locale)}",
                data = "/check_wishlist"
            )
        }
    }

    fun subscribeMenuKeyboard(locale: String): InlineKeyboardMarkup = inlineKeyboard {
        row {
            dataButton(
                text = "🚫 ${Localization.getButton("unsubscribe", locale)}",
                data = "/unsubscribe"
            )
            dataButton(
                text = "🔗 ${Localization.getButton("links_to_game", locale)}",
                data = "/links_to_game"
            )
        }
    }
}