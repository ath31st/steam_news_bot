package sidim.doma.application.news.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SteamImageUrlResolverTest {
    private val resolver = SteamImageUrlResolver()

    @Test
    fun resolvesSteamClanImageToken() {
        val url = resolver.resolveClanImage("{STEAM_CLAN_IMAGE}/18499191/d02a57c07e02.png")
        assertEquals(
            "https://clan.fastly.steamstatic.com/images/18499191/d02a57c07e02.png",
            url
        )
    }

    @Test
    fun returnsNullForInvalidClanImageToken() {
        assertNull(resolver.resolveClanImage("{STEAM_OTHER}/abc"))
    }

    @Test
    fun extractsMultipleImageUrls() {
        val content = """
            {STEAM_CLAN_IMAGE}/123/abc.png
            [img]https://cdn.example.com/image.png[/img]
            https://cdn.example.com/photo.jpg
        """.trimIndent()

        val urls = resolver.extractImageUrls(content)
        assertEquals(3, urls.size)
        assertTrue(urls[0].contains("clan.fastly.steamstatic.com"))
        assertEquals("https://cdn.example.com/image.png", urls[1])
        assertEquals("https://cdn.example.com/photo.jpg", urls[2])
    }
}
