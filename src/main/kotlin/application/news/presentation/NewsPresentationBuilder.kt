package sidim.doma.application.news.presentation

import sidim.doma.application.news.parser.SteamContentParser
import sidim.doma.application.news.parser.SteamContentToRichBlocksMapper
import sidim.doma.application.news.parser.splitBlocks
import sidim.doma.common.util.LocalizationUtils.getText
import dev.inmo.tgbotapi.requests.abstracts.InputFile
import dev.inmo.tgbotapi.types.media.TelegramMediaPhoto
import dev.inmo.tgbotapi.types.rich.InputRichMessage
import dev.inmo.tgbotapi.types.rich.InputRichMessageBlocks
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsPresentationBuilder(
    private val contentParser: SteamContentParser,
    private val richBlocksMapper: SteamContentToRichBlocksMapper
) {
    fun build(context: NewsPresentationContext): InputRichMessage {
        val parsed = contentParser.parse(context.newsItem.contents)
        val split = splitBlocks(parsed.blocks, MAX_VISIBLE_PLAIN_TEXT_LENGTH)
        val visibleRich = richBlocksMapper.toRichBlocks(split.visible, parsed.heroImageUrl)

        return InputRichMessageBlocks {
            buildHeader(context)
            divider()
            h3(context.newsItem.title)
            buildMetaLine(context)?.let { paragraph { italic(it) } }
            buildHeroPhoto(parsed.heroImageUrl)?.let { photo(it) }
            visibleRich.forEach { add(it) }
            if (split.hasFolded) {
                val foldedRich = richBlocksMapper.toRichBlocks(split.folded, heroImageUrl = null)
                details(
                    summary = getText("news.expand", context.locale),
                    isOpen = false
                ) {
                    foldedRich.forEach { add(it) }
                }
            }
            divider()
            footer(formatDate(context.newsItem.date, context.locale))
            paragraph {
                url(
                    "🔗 ${getText("news.read_more", context.locale)} →",
                    context.newsItem.url
                )
            }
        }
    }

    private fun dev.inmo.tgbotapi.types.rich.InputRichBlocksBuilder.buildHeader(context: NewsPresentationContext) {
        h2 {
            if (context.gameName != null) {
                plain("🎮 ${context.gameName}")
            }
            if (context.isInWishlist) {
                plain(" · ${getText("news.in_wishlist", context.locale)}")
            }
        }
    }

    private fun buildMetaLine(context: NewsPresentationContext): String? {
        val parts = listOfNotNull(
            context.newsItem.feedLabel.takeIf { it.isNotBlank() },
            context.newsItem.author.takeIf { it.isNotBlank() }?.let { "by $it" }
        )
        return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }

    private fun buildHeroPhoto(imageUrl: String?): TelegramMediaPhoto? =
        imageUrl?.let { TelegramMediaPhoto(InputFile.fromUrl(it)) }

    private fun formatDate(unixSeconds: Long, locale: String): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag(locale))
        return "📅 ${getText("news.published", locale)}: ${formatter.format(Date(unixSeconds * 1000))}"
    }

    companion object {
        const val MAX_VISIBLE_PLAIN_TEXT_LENGTH = 500
    }
}
