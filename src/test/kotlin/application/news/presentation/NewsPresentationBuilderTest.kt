package sidim.doma.application.news.presentation

import sidim.doma.application.news.parser.SteamContentParser
import sidim.doma.application.news.parser.SteamContentToRichBlocksMapper
import sidim.doma.domain.news.entity.NewsItem
import dev.inmo.tgbotapi.types.rich.InputRichBlockDetails
import dev.inmo.tgbotapi.types.rich.InputRichBlockDivider
import dev.inmo.tgbotapi.types.rich.InputRichBlockFooter
import dev.inmo.tgbotapi.types.rich.InputRichBlockParagraph
import dev.inmo.tgbotapi.types.rich.InputRichBlockSectionHeading
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NewsPresentationBuilderTest {
    private val builder = NewsPresentationBuilder(
        SteamContentParser(),
        SteamContentToRichBlocksMapper()
    )

    @Test
    fun buildsRichMessageWithHeaderAndFooter() {
        val message = builder.build(sampleContext())
        val blocks = requireNotNull(message.blocks)

        assertTrue(blocks.any { it is InputRichBlockSectionHeading })
        assertTrue(blocks.any { it is InputRichBlockDivider })
        assertTrue(blocks.any { it is InputRichBlockFooter })
        assertTrue(blocks.any { it is InputRichBlockParagraph })
    }

    @Test
    fun includesWishlistBadgeInHeader() {
        val message = builder.build(sampleContext(isInWishlist = true))
        val heading = message.blocks.orEmpty()
            .filterIsInstance<InputRichBlockSectionHeading>()
            .first()

        assertTrue(heading.text.toString().contains("wishlist", ignoreCase = true))
    }

    @Test
    fun foldsLongBodyUnderDetails() {
        val longPlainText = "Changes" + "The Celadon Flame Brewers. ".repeat(80)
        val message = builder.build(
            sampleContext(
                newsItem = sampleNewsItem(contents = longPlainText)
            )
        )

        val blocks = requireNotNull(message.blocks)
        assertTrue(blocks.any { it is InputRichBlockDetails })
    }

    @Test
    fun keepsShortBodyWithoutDetails() {
        val message = builder.build(sampleContext())

        val blocks = requireNotNull(message.blocks)
        assertFalse(blocks.any { it is InputRichBlockDetails })
    }

    private fun sampleContext(
        newsItem: NewsItem = sampleNewsItem(),
        gameName: String? = "Test Game",
        isInWishlist: Boolean = false,
        locale: String = "en"
    ) = NewsPresentationContext(
        newsItem = newsItem,
        gameName = gameName,
        isInWishlist = isInWishlist,
        locale = locale
    )

    private fun sampleNewsItem(
        contents: String = "[p]Hello [b]world[/b][/p]"
    ) = NewsItem(
        gid = 1L,
        title = "Patch 1.0",
        url = "https://store.steampowered.com/news/app/570/view/123",
        author = "Valve",
        feedLabel = "Patch Notes",
        contents = contents,
        appid = "570",
        date = 1_725_000_000L
    )
}
