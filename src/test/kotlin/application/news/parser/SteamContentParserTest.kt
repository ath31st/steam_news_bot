package sidim.doma.application.news.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SteamContentParserTest {
    private val parser = SteamContentParser()

    @Test
    fun parsesBoldAndItalicInline() {
        val result = parser.parse("[p]Text with [b]bold[/b] and [i]italic[/i][/p]")
        val paragraph = result.blocks.single() as SteamContentNode.Paragraph
        assertTrue(paragraph.children.any { it is SteamContentNode.Bold })
        assertTrue(paragraph.children.any { it is SteamContentNode.Italic })
    }

    @Test
    fun parsesHeading() {
        val result = parser.parse("[h1]Title[/h1]")
        val heading = result.blocks.single() as SteamContentNode.Heading
        assertEquals(1, heading.level)
        assertEquals("Title", plainTextOf(heading))
    }

    @Test
    fun parsesListItems() {
        val result = parser.parse("[list][*]Item 1[*]Item 2[/list]")
        val list = result.blocks.single() as SteamContentNode.ListBlock
        assertEquals(2, list.items.size)
        assertEquals("Item 1", list.items[0].joinToString("") { plainTextOf(it) })
        assertEquals("Item 2", list.items[1].joinToString("") { plainTextOf(it) })
    }

    @Test
    fun parsesQuoteBlock() {
        val result = parser.parse("[quote]Quoted text[/quote]")
        assertIs<SteamContentNode.Quote>(result.blocks.single())
    }

    @Test
    fun parsesUrlLink() {
        val result = parser.parse("[url=https://store.steampowered.com]Visit Steam[/url]")
        val link = findFirstNode(result.blocks, SteamContentNode.Link::class.java)
        assertNotNull(link)
        assertEquals("https://store.steampowered.com", link.href)
        assertEquals("Visit Steam", plainTextOf(link))
    }

    @Test
    fun extractsHeroImageFromSteamToken() {
        val result = parser.parse("{STEAM_CLAN_IMAGE}/18499191/d02a57c07e02.png[p]After image[/p]")
        assertNotNull(result.heroImageUrl)
        assertTrue(result.heroImageUrl!!.contains("18499191"))
        assertTrue(result.blocks.any { it is SteamContentNode.Paragraph })
    }

    @Test
    fun removesRelatedLinksSection() {
        val result = parser.parse("[h1]Visible[/h1]RELATED LINKS: hidden content")
        assertEquals("Visible", plainTextOf(result.blocks.single()))
    }

    @Test
    fun parsesHtmlParagraphWithStrongAndLink() {
        val result = parser.parse("""<p>HTML with <strong>strong</strong> and <a href="https://example.com">link</a></p>""")
        val paragraph = result.blocks.single() as SteamContentNode.Paragraph
        assertTrue(paragraph.children.any { it is SteamContentNode.Bold })
        assertTrue(paragraph.children.any { it is SteamContentNode.Link })
    }

    @Test
    fun parsesPreformattedBlock() {
        val result = parser.parse("[pre]code block[/pre]")
        val pre = result.blocks.single() as SteamContentNode.Pre
        assertEquals("code block", pre.text)
    }

    @Test
    fun parsesStrikeAndUnderline() {
        val result = parser.parse("[u]underlined[/u] and [strike]removed[/strike]")
        val paragraph = result.blocks.single() as SteamContentNode.Paragraph
        assertTrue(paragraph.children.any { it is SteamContentNode.Underline })
        assertTrue(paragraph.children.any { it is SteamContentNode.Strike })
    }

    @Test
    fun parsesFixtureFile() {
        val fixture = readFixture("mixed_content.txt")
        val result = parser.parse(fixture)
        assertTrue(result.blocks.isNotEmpty())
        assertNotNull(result.heroImageUrl)
    }

    @Test
    fun truncateBlocksRespectsLimit() {
        val blocks = listOf(
            SteamContentNode.Paragraph(listOf(SteamContentNode.Text("a".repeat(800)))),
            SteamContentNode.Paragraph(listOf(SteamContentNode.Text("b".repeat(800))))
        )
        val (truncatedBlocks, truncated) = truncateBlocks(blocks, 1000)
        assertTrue(truncated)
        assertEquals(2, truncatedBlocks.size)
        assertEquals(800, plainTextOf(truncatedBlocks[0]).length)
        assertEquals(200, plainTextOf(truncatedBlocks[1]).replace("…", "").length)
    }

    @Test
    fun truncateBlocksShowsPartialWhenSingleBlockExceedsLimit() {
        val blocks = listOf(
            SteamContentNode.Paragraph(listOf(SteamContentNode.Text("word ".repeat(400))))
        )
        val (truncatedBlocks, truncated) = truncateBlocks(blocks, 1000)

        assertTrue(truncated)
        assertEquals(1, truncatedBlocks.size)
        val text = plainTextOf(truncatedBlocks.single())
        assertTrue(text.length <= 1001)
        assertTrue(text.endsWith("…"))
        assertTrue(text.startsWith("word"))
    }

    @Test
    fun truncateBlocksKeepsShortContent() {
        val blocks = listOf(
            SteamContentNode.Paragraph(listOf(SteamContentNode.Text("short")))
        )
        val (truncatedBlocks, truncated) = truncateBlocks(blocks, 1000)
        assertFalse(truncated)
        assertEquals(1, truncatedBlocks.size)
    }

    @Test
    fun splitBlocksKeepsShortContentWithoutFold() {
        val blocks = listOf(
            SteamContentNode.Paragraph(listOf(SteamContentNode.Text("short")))
        )
        val split = splitBlocks(blocks, 500)

        assertFalse(split.hasFolded)
        assertEquals(1, split.visible.size)
        assertTrue(split.folded.isEmpty())
    }

    @Test
    fun splitBlocksFoldsLongContent() {
        val blocks = listOf(
            SteamContentNode.Paragraph(listOf(SteamContentNode.Text("a".repeat(400)))),
            SteamContentNode.Paragraph(listOf(SteamContentNode.Text("b".repeat(400))))
        )
        val split = splitBlocks(blocks, 500)

        assertTrue(split.hasFolded)
        assertTrue(split.visible.isNotEmpty())
        assertTrue(split.folded.isNotEmpty())
        assertTrue(plainTextOf(split.visible.first()).length <= 500)
    }

    @Test
    fun splitBlocksSplitsWithinSingleParagraph() {
        val blocks = listOf(
            SteamContentNode.Paragraph(listOf(SteamContentNode.Text("word ".repeat(400))))
        )
        val split = splitBlocks(blocks, 500)

        assertTrue(split.hasFolded)
        assertEquals(1, split.visible.size)
        assertEquals(1, split.folded.size)
        assertTrue(plainTextOf(split.visible.single()).length <= 500)
        assertTrue(plainTextOf(split.folded.single()).isNotEmpty())
    }

    private fun <T : SteamContentNode> findFirstNode(blocks: List<SteamContentNode>, type: Class<T>): T? {
        for (block in blocks) {
            if (type.isInstance(block)) {
                @Suppress("UNCHECKED_CAST")
                return block as T
            }
            val nested = nestedNodes(block)
            findFirstNode(nested, type)?.let { return it }
        }
        return null
    }

    private fun nestedNodes(block: SteamContentNode): List<SteamContentNode> = when (block) {
        is SteamContentNode.Paragraph -> block.children
        is SteamContentNode.Heading -> block.children
        is SteamContentNode.Quote -> block.children
        is SteamContentNode.Bold -> block.children
        is SteamContentNode.Italic -> block.children
        is SteamContentNode.Underline -> block.children
        is SteamContentNode.Strike -> block.children
        is SteamContentNode.Link -> block.children
        is SteamContentNode.Code -> block.children
        else -> emptyList()
    }

    private fun readFixture(name: String): String =
        checkNotNull(javaClass.classLoader.getResource("steam_news_samples/$name")).readText()
}
