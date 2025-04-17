package sidim.doma.domain.news.service

import sidim.doma.common.util.LocalizationUtils
import sidim.doma.domain.news.entity.NewsItem
import java.text.SimpleDateFormat
import java.util.*

class NewsItemService {
    companion object {
        const val MAX_MESSAGE_LENGTH = 4000
    }

    fun formatNewsForTelegram(newsItem: NewsItem, gameName: String?, locale: String): String {
        val name = if (gameName != null) "<b><u>$gameName</u></b>\n" else ""
        val newsTitle = "<b>${newsItem.title}</b>\n\n"
        val url =
            "\n<a href=\"${newsItem.url}\">${
                LocalizationUtils.getText(
                    "news.read_more",
                    locale
                )
            }</a>"
        val date = "\n<i>${
            LocalizationUtils.getText(
                "news.published",
                locale
            )
        }: ${convertUnixToDate(newsItem.date.toLong(), locale)}</i>"

        val fixedLength = name.length + newsTitle.length + date.length + url.length + 2
        val maxContentLength = MAX_MESSAGE_LENGTH - fixedLength

        val formattedContent = formatContent(newsItem.contents)
        val truncatedContent = if (formattedContent.length <= maxContentLength) {
            formattedContent
        } else {
            val truncated = formattedContent.substring(0, maxContentLength - 50)
            "$truncated...\n<i>${LocalizationUtils.getText("news.truncated", locale)}</i>"
        }

        return name + newsTitle + truncatedContent + date + url
    }

    private fun formatContent(content: String): String {
        var result = content

        result = result.replace(Regex("<img\\s+[^>]*>"), "")
            .replace(Regex("\\[img].*?\\[/img]"), "")
            .replace(Regex("\\{STEAM[^}]*\\.(jpg|png|gif)\\b"), "")
            .replace(Regex("\\{STEAM_CLAN_IMAGE}/\\d+/[a-f0-9]+(?:\\.(png|jpg|gif))?"), "")
            .replace(Regex("https://i\\.imgur\\.com/\\S+"), "")
            .replace(Regex("https://i\\.ibb\\.co/\\S+"), "")

        result = result.replace(Regex("<br\\s*/?>"), "\n")
            .replace(Regex("</?p>"), "\n")
            .replace(Regex("<strong>"), "<b>")
            .replace(Regex("</strong>"), "</b>")

        result = result.replace("[h1]", "<b>").replace("[/h1]", "</b>\n")
            .replace("[h2]", "<b>").replace("[/h2]", "</b>\n")
            .replace("[h3]", "<b>").replace("[/h3]", "</b>\n")
            .replace("[b]", "<b>").replace("[/b]", "</b>")
            .replace("[i]", "<i>").replace("[/i]", "</i>")

        result = result.replace("[list]", "").replace("[/list]", "")
            .replace(Regex("\\[\\*]"), "• ")

        result = result.replace(Regex("\\[url=(.*?)](.*?)\\[/url]"), "<a href=\"$1\">$2</a>")
            .replace(Regex("<a\\s+href=\"([^\"]+)\"[^>]*>(.*?)</a>"), "<a href=\"$1\">$2</a>")

        result = result.replace(Regex("\n{2,}"), "\n\n")
            .replace(Regex("\\s{2,}"), " ")
            .trim()

        result = result.replace(Regex("RELATED LINKS:.*", RegexOption.DOT_MATCHES_ALL), "")

        return result.trim()
    }

    private fun convertUnixToDate(unixSeconds: Long, locale: String): String {
        val date = Date(unixSeconds * 1000)

        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag(locale))
        return formatter.format(date)
    }
}