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
                text = "λ ${Localization.getButton("button.set_upd_steam_id", locale)}",
                data = "/set_steam_id"
            )
            dataButton(
                text = "Ω ${Localization.getButton("button.check_steam_id", locale)}",
                data = "/check_steam_id"
            )
        }
        row {
            dataButton(
                text = "✅ ${Localization.getButton("button.set_active_mode", locale)}",
                data = "/set_active_mode"
            )
            dataButton(
                text = "☑ ${Localization.getButton("button.set_inactive_mode", locale)}",
                data = "/set_inactive_mode"
            )
        }
        row {
            dataButton(
                text = "🧹 ${Localization.getButton("button.clear_black_list", locale)}",
                data = "/clear_black_list"
            )
            dataButton(
                text = "🗑 ${Localization.getButton("button.black_list", locale)}",
                data = "/black_list"
            )
        }
        row {
            dataButton(
                text = "♥ ${Localization.getButton("button.check_wishlist", locale)}",
                data = "/check_wishlist"
            )
        }
    }

    fun subscribeMenuKeyboard(locale: String): InlineKeyboardMarkup = inlineKeyboard {
        row {
            dataButton(
                text = "🚫 ${Localization.getButton("button.unsubscribe", locale)}",
                data = "/unsubscribe"
            )
            dataButton(
                text = "🔗 ${Localization.getButton("button.links_to_game", locale)}",
                data = "/links_to_game"
            )
        }
    }
}