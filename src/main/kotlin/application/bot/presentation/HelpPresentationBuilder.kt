package sidim.doma.application.bot.presentation

import dev.inmo.tgbotapi.types.rich.InputRichMessage
import dev.inmo.tgbotapi.types.rich.InputRichMessageBlocks
import sidim.doma.common.util.LocalizationUtils.getText

class HelpPresentationBuilder {

    fun build(locale: String): InputRichMessage = InputRichMessageBlocks {
        h2(getText("help.title", locale))

        h3(getText("help.how_title", locale))
        unorderedList {
            item(getText("help.how_news", locale))
            item(getText("help.how_library", locale))
        }

        divider()

        h3(getText("help.commands_title", locale))
        unorderedList {
            item(getText("help.commands_start", locale))
            item(getText("help.commands_settings", locale))
            item(getText("help.commands_stats", locale))
            item(getText("help.commands_help", locale))
        }

        divider()

        h3(getText("help.steamid_title", locale))
        paragraph(getText("help.steamid_body", locale))
        paragraph {
            url(
                getText("help.steamid_link_label", locale),
                STEAM_ACCOUNT_URL
            )
        }
        paragraph {
            code(getText("help.steamid_example", locale))
        }

        divider()

        h3(getText("help.wishlist_title", locale))
        paragraph(getText("help.wishlist_body", locale))
        unorderedList {
            item(getText("help.wishlist_profile", locale))
            item(getText("help.wishlist_games", locale))
        }
        paragraph {
            url(
                getText("help.wishlist_link_label", locale),
                STEAM_PRIVACY_URL
            )
        }

        divider()

        h3(getText("help.blacklist_title", locale))
        paragraph(getText("help.blacklist_body", locale))

        divider()

        paragraph(getText("help.feedback", locale))
    }

    companion object {
        const val STEAM_ACCOUNT_URL = "https://store.steampowered.com/account/"
        const val STEAM_PRIVACY_URL = "https://steamcommunity.com/my/edit/settings"
    }
}
