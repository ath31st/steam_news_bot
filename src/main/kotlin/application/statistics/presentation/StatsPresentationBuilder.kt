package sidim.doma.application.statistics.presentation

import dev.inmo.tgbotapi.types.rich.InputRichMessage
import dev.inmo.tgbotapi.types.rich.InputRichMessageMarkdown
import sidim.doma.application.statistics.dto.CommonStatistics
import sidim.doma.application.statistics.dto.NewsStatistics
import sidim.doma.application.statistics.dto.UserStatistics
import sidim.doma.common.util.LocalizationUtils.getText

class StatsPresentationBuilder {

    fun buildRichMessage(
        common: CommonStatistics,
        news: NewsStatistics,
        locale: String,
        userStats: UserStatistics? = null,
    ): InputRichMessage = InputRichMessageMarkdown(buildMarkdownTable(common, news, locale, userStats))

    private fun buildMarkdownTable(
        common: CommonStatistics,
        news: NewsStatistics,
        locale: String,
        userStats: UserStatistics?,
    ): String {
        return buildString {
            appendLine("## ${getText("stats.title", locale)}")
            appendLine()
            appendLine("|  |  |")
            appendLine("| :------ | -----: |")
            appendMetricRow(locale, "stats.row.total_users", common.countUsers)
            appendMetricRow(locale, "stats.row.active_users", common.countActiveUsers)
            appendMetricRow(locale, "stats.row.games", common.countGames)
            if (userStats != null) {
                appendMetricRow(locale, "stats.row.games_yours", userStats.ownedGames)
                appendMetricRow(locale, "stats.row.games_wishlist", userStats.wishlistGames)
            }
            appendLine("|  |  |")
            appendMetricRow(locale, "stats.row.news_today", news.dailyCountNews)
            appendMetricRow(locale, "stats.row.news_total", news.totalCountNews)
        }.trimEnd()
    }

    private fun StringBuilder.appendMetricRow(
        locale: String,
        labelKey: String,
        amount: Number,
    ) {
        appendLine("| ${getText(labelKey, locale)} | **$amount** |")
    }
}
