package sidim.doma.application.statistics.presentation

import sidim.doma.application.statistics.dto.CommonStatistics
import sidim.doma.application.statistics.dto.NewsStatistics
import sidim.doma.application.statistics.dto.UserStatistics
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatsPresentationBuilderTest {
    private val builder = StatsPresentationBuilder()

    @Test
    fun buildsMarkdownTableWithMetrics() {
        val markdown = builder.buildRichMessage(sampleCommon(), sampleNews(), "en")
            .markdown.orEmpty()

        assertContains(markdown, "## 📊 Statistics")
        assertFalse(markdown.contains("| Metric | Value |"))
        assertContains(markdown, "| :------ | -----: |")
        assertContains(markdown, "| 👥 Total users | **10** |")
        assertContains(markdown, "| 📰 News all time | **500** |")
        assertContains(markdown, "|  |  |")
        assertFalse(markdown.contains("—"))
    }

    @Test
    fun includesUserStatsWhenProvided() {
        val markdown = builder.buildRichMessage(
            sampleCommon(),
            sampleNews(),
            "en",
            UserStatistics(ownedGames = 42, wishlistGames = 15),
        ).markdown.orEmpty()

        assertContains(markdown, "| 🎮 Of them, yours: | **42** |")
        assertContains(markdown, "| ⭐ In wishlist: | **15** |")
    }

    @Test
    fun omitsUserStatsWhenNotProvided() {
        val markdown = builder.buildRichMessage(sampleCommon(), sampleNews(), "en")
            .markdown.orEmpty()

        assertFalse(markdown.contains("Of them, yours:"))
        assertFalse(markdown.contains("In wishlist:"))
    }

    @Test
    fun buildsHtmlFallbackWithPreformattedTable() {
        val html = builder.buildFallbackHtml(sampleCommon(), sampleNews(), "ru")

        assertContains(html, "<b>")
        assertContains(html, "<pre>")
        assertContains(html, "👥 Всего пользователей")
    }

    @Test
    fun escapesHtmlInFallback() {
        val html = builder.buildFallbackHtml(sampleCommon(), sampleNews(), "en")
        assertTrue(!html.contains("<pre>| Metric | Value |</pre>") || html.contains("&lt;") || html.contains("<pre>"))
    }

    private fun sampleCommon() = CommonStatistics(
        countUsers = 10,
        countActiveUsers = 4,
        countGames = 250
    )

    private fun sampleNews() = NewsStatistics(
        dailyCountNews = 12,
        totalCountNews = 500
    )
}
