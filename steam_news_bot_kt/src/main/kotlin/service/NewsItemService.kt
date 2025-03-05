package sidim.doma.service

import sidim.doma.entity.NewsItem
import sidim.doma.plugin.Localization
import java.text.SimpleDateFormat
import java.util.*

class NewsItemService {
    companion object {
        const val MAX_MESSAGE_LENGTH = 4000
    }

    fun formatNewsForTelegram(newsItem: NewsItem, locale: String): String {
        val title = "<b>${newsItem.title}</b>\n\n"
        val formattedContent = formatContent(newsItem.contents)
        val url =
            "\n<a href=\"${newsItem.url}\">${Localization.getText("news.read_more", locale)}</a>"
        val date = "\n<i>${
            Localization.getText(
                "news.published",
                locale
            )
        }: ${convertUnixToDate(newsItem.date.toLong(), locale)}</i>"

        val fullMessage = title + formattedContent + url + date
        return if (fullMessage.length <= MAX_MESSAGE_LENGTH) fullMessage else truncateMessage(
            fullMessage,
            locale
        )
    }

    private fun formatContent(content: String): String {
        var result = content

        result = result.replace(Regex("<img\\s+[^>]*>"), "")
            .replace(Regex("\\[img].*?\\[/img]"), "")
            .replace(Regex("\\{STEAM_CLAN_IMAGE}/\\d+/[a-f0-9]+\\.(png|jpg|gif)"), "")

        result = result.replace(Regex("<br\\s*/?>"), "\n")
            .replace(Regex("</?p>"), "\n")
            .replace(Regex("<strong>"), "<b>").replace(Regex("</strong>"), "</b>")

        result = result.replace("[h2]", "<b>").replace("[/h2]", "</b>\n")
            .replace("[h3]", "<b>").replace("[/h3]", "</b>\n")
            .replace(Regex("\\[url=(.*?)](.*?)\\[/url]"), "<a href=\"$1\">$2</a>")

        result =
            result.replace(Regex("<a\\s+href=\"([^\"]+)\"[^>]*>(.*?)</a>"), "<a href=\"$1\">$2</a>")

        result = result.replace(Regex("\n{2,}"), "\n\n")
            .replace(Regex("\\s{2,}"), " ")
            .trim()

        result = result.replace(Regex("RELATED LINKS:.*", RegexOption.DOT_MATCHES_ALL), "")

        return result
    }

    private fun convertUnixToDate(unixSeconds: Long, locale: String): String {
        val date = Date(unixSeconds * 1000)
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag(locale))
        return formatter.format(date)
    }

    private fun truncateMessage(message: String, locale: String): String {
        return message.substring(0, MAX_MESSAGE_LENGTH) + "...\n<i>${
            Localization.getText(
                "news.truncated",
                locale
            )
        }</i>"
    }
}