package sidim.doma.application.news.presentation

import sidim.doma.common.util.LocalizationUtils.getText
import sidim.doma.domain.news.entity.NewsItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsFallbackFormatter(
    private val presentationBuilder: NewsPresentationBuilder
) {
    fun format(
        context: NewsPresentationContext
    ): String {
        val news = context.newsItem
        val parts = buildList {
            context.gameName?.let { add("<b><u>${escapeHtml(it)}</u></b>") }
            if (context.isInWishlist) {
                add(getText("news.in_wishlist", context.locale))
            }
            add("<b>${escapeHtml(news.title)}</b>")
            buildMetaLine(context)?.let { add("<i>${escapeHtml(it)}</i>") }
            val excerpt = presentationBuilder.buildPlainTextExcerpt(news)
            if (excerpt.isNotBlank()) {
                add("<blockquote>${escapeHtml(excerpt)}</blockquote>")
            }
            add(
                "<i>${getText("news.published", context.locale)}: ${
                    formatDate(news.date, context.locale)
                }</i>"
            )
            add(
                "<a href=\"${escapeHtml(news.url)}\">${getText("news.read_more", context.locale)}</a>"
            )
        }
        return parts.joinToString("\n\n")
    }

    private fun buildMetaLine(context: NewsPresentationContext): String? {
        val parts = listOfNotNull(
            context.newsItem.feedLabel.takeIf { it.isNotBlank() },
            context.newsItem.author.takeIf { it.isNotBlank() }?.let { "by $it" }
        )
        return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }

    private fun formatDate(unixSeconds: Long, locale: String): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag(locale))
        return formatter.format(Date(unixSeconds * 1000))
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
