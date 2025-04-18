package sidim.doma.infrastructure.integration.steam

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sidim.doma.domain.game.entity.Game
import sidim.doma.domain.news.entity.NewsItem
import java.util.regex.Pattern

class SteamApiClient(
    private val apiKey: String,
    private val client: HttpClient,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private const val USER_AGENT = "Mozilla/5.0"
        private const val BASE_API_URL = "http://api.steampowered.com"
        private const val BASE_STORE_URL = "http://store.steampowered.com"
        private const val OWNED_GAMES_PATH = "/IPlayerService/GetOwnedGames/v1/"
        private const val NEWS_PATH = "/ISteamNews/GetNewsForApp/v2/"
        private const val WISHLIST_PATH = "/IWishlistService/GetWishlist/v1/"
        private const val APP_DETAILS_PATH = "$BASE_STORE_URL/api/appdetails"

        private val STEAM_ID_PATTERN = Pattern.compile("765\\d{14}")

        private val INVALID_RESPONSES = listOf(
            """{"response":{}}""",
            """{"success": 2}"""
        )
    }

    suspend fun getAppDetails(appId: String): Game? {
        val response = fetch(APP_DETAILS_PATH) {
            parameter("appids", appId)
            parameter("filters", "basic")
        }
        return when {
            response in INVALID_RESPONSES -> null
            else -> try {
                parseAppDetails(response)
            } catch (_: Exception) {
                null
            }
        }
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
                throw IllegalStateException("Account with steamId $steamId is hidden or does not exist")

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

    suspend fun getNewsByAppid(
        appId: String,
        newsCount: Int,
        maxLength: Int
    ): List<NewsItem> {
        val response = fetch("$BASE_API_URL$NEWS_PATH") {
            parameter("appid", appId)
            parameter("count", newsCount)
            parameter("maxlength", maxLength)
        }
        return parseNewsItems(response)
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

    private fun parseAppDetails(json: String): Game = objectMapper.readTree(json)
        .let { rootNode ->
            val appEntry = rootNode.fields().next()
            val dataNode = appEntry.value.path("data")

            require(!dataNode.isEmpty && dataNode.path("success").asBoolean(true))

            Game(
                appid = appEntry.key,
                name = dataNode.path("name").asText()
            )
        }

    private fun parseWishlistGames(json: String): List<Game> = objectMapper.readTree(json)
        .path("response")["items"]
        .let { itemsNode ->
            if (itemsNode.isArray) itemsNode.map { objectMapper.convertValue(it, Game::class.java) }
            else emptyList()
        }

    private fun parseNewsItems(json: String): List<NewsItem> =
        objectMapper.readTree(json)
            .path("appnews")["newsitems"]
            .let { newsNode ->
                if (newsNode.isArray) {
                    newsNode.mapNotNull { objectMapper.convertValue<NewsItem>(it) }
                } else emptyList()
            }
}