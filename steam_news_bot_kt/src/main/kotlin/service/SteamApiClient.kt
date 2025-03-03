package sidim.doma.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sidim.doma.entity.Game
import sidim.doma.entity.NewsItem
import java.util.regex.Pattern

class SteamApiClient(
    private val apiKey: String,
    private val client: HttpClient,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private const val USER_AGENT = "Mozilla/5.0"
        private const val BASE_API_URL = "http://api.steampowered.com"
        private const val OWNED_GAMES_PATH = "/IPlayerService/GetOwnedGames/v1/"
        private const val NEWS_PATH = "/ISteamNews/GetNewsForApp/v2/"
        private const val WISHLIST_PATH = "/IWishlistService/GetWishlist/v1/"

        private const val FILTER_WINDOW_IN_SECONDS: Long = 180000L
        private const val NEW_COUNT_LIMIT: Int = 3
        private const val MAX_LENGTH_FOR_CONTENT: Int = 300

        private val STEAM_ID_PATTERN = Pattern.compile("765\\d{14}")
        private val IMAGE_LINK_PATTERN =
            Pattern.compile("\\{STEAM.*((.jpg)|(.png)|(.gif))\\b|\\{STEAM.*")

        private val INVALID_RESPONSES = listOf(
            """{"response":{}}""",
            """{"success": 2}"""
        )
    }

    suspend fun getOwnedGames(steamId: String): List<Game> {
        val response = fetch("$BASE_API_URL$OWNED_GAMES_PATH") {
            parameter("skip_unvetted_apps", "true")
            parameter("key", apiKey)
            parameter("include_appinfo", "true")
            parameter("steamid", steamId)
        }
        return when {
            response in INVALID_RESPONSES ->
                throw IllegalStateException("Account is hidden or does not exist")

            else -> parseGames(response)
        }
    }

    suspend fun getWishlistGames(steamId: String): List<Game> {
        val response = fetch("$BASE_API_URL$WISHLIST_PATH") {
            parameter("steamid", steamId)
        }
        return when {
            response in INVALID_RESPONSES -> emptyList()

            else -> parseWishlistGames(response)
        }
    }

    suspend fun getRecentNewsByOwnedGames(
        appId: String,
        newsCount: Int = NEW_COUNT_LIMIT,
        maxLength: Int = MAX_LENGTH_FOR_CONTENT
    ): List<NewsItem> {
        val response = fetch("$BASE_API_URL$NEWS_PATH") {
            parameter("appid", appId)
            parameter("count", newsCount)
            parameter("maxlength", maxLength)
        }
        return parseNewsItemsWithTimeFilter(response)
    }

    fun isValidSteamId(steamId: String): Boolean = STEAM_ID_PATTERN.matcher(steamId).matches()

    suspend fun checkWishlistAvailability(steamId: Long): Int {
        val response = fetch("$BASE_API_URL$WISHLIST_PATH") {
            parameter("steamid", steamId)
        }
        return when {
            response in INVALID_RESPONSES -> 500

            else -> 200
        }
    }

    private suspend fun fetch(
        url: String,
        params: (HttpRequestBuilder.() -> Unit)? = null
    ): String {
        return client.get(url) {
            header(HttpHeaders.UserAgent, USER_AGENT)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            params?.invoke(this)
        }.bodyAsText()
    }

    private fun parseGames(json: String): List<Game> = objectMapper.readTree(json)
        .path("response")["games"]
        .let { gamesNode ->
            if (gamesNode.isArray) gamesNode.map { objectMapper.convertValue(it, Game::class.java) }
            else emptyList()
        }

    private fun parseWishlistGames(json: String): List<Game> = objectMapper.readTree(json)
        .path("response")["items"]
        .let { itemsNode ->
            if (itemsNode.isArray) itemsNode.map { objectMapper.convertValue(it, Game::class.java) }
            else emptyList()
        }

    private fun parseNewsItemsWithTimeFilter(json: String): List<NewsItem> =
        objectMapper.readTree(json)
            .path("appnews")["newsitems"]
            .let { newsNode ->
                if (newsNode.isArray) {
                    newsNode.mapNotNull { node ->
                        val dateSeconds = node["date"].asLong()
                        if (isRecentNews(dateSeconds)) {
                            objectMapper.convertValue<NewsItem>(node).apply {
                                contents = removeImageLinks(contents)
                            }
                        } else null
                    }
                } else emptyList()
            }

    private fun isRecentNews(seconds: Long, limit: Long = FILTER_WINDOW_IN_SECONDS): Boolean {
        val currentTimeSeconds = System.currentTimeMillis() / 1000
        return seconds >= (currentTimeSeconds - limit)
    }

    private fun removeImageLinks(text: String): String =
        IMAGE_LINK_PATTERN.matcher(text).replaceAll("")
}