package sidim.doma.application.news.parser

class SteamImageUrlResolver {
    private val steamClanImageRegex = Regex("""\{STEAM_CLAN_IMAGE\}(/[\w./-]+)""", RegexOption.IGNORE_CASE)
    private val directImageUrlRegex = Regex("""https://\S+\.(?:jpg|jpeg|png|gif|webp)(?:\?\S*)?""", RegexOption.IGNORE_CASE)

    fun resolveClanImage(token: String): String? {
        val path = steamClanImageRegex.find(token)?.groupValues?.get(1) ?: return null
        return "$STEAM_CLAN_CDN_BASE$path"
    }

    fun resolveImgTag(url: String): String = url.trim()

    fun resolveDirectUrl(url: String): String = url.trim()

    fun extractImageUrls(content: String): List<String> = buildList {
        steamClanImageRegex.findAll(content).forEach { match ->
            resolveClanImage(match.value)?.let { add(it) }
        }
        Regex("""\[img\](.*?)\[/img]""", RegexOption.IGNORE_CASE).findAll(content).forEach { match ->
            add(resolveImgTag(match.groupValues[1]))
        }
        directImageUrlRegex.findAll(content).forEach { match ->
            add(resolveDirectUrl(match.value))
        }
    }.distinct()

    companion object {
        const val STEAM_CLAN_CDN_BASE = "https://clan.fastly.steamstatic.com/images"
    }
}
