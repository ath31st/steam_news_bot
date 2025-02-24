package sidim.doma.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.koin.core.parameter.parametersOf
import sidim.doma.entity.Game
import sidim.doma.entity.NewsItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

class SteamApiClient(
    private val apiKey: String,
    private val client: HttpClient,
    private val objectMapper: ObjectMapper
) {
    companion object {
        private const val USER_AGENT = "Mozilla/5.0"

        private const val BASE_API_URL = "http://api.steampowered.com"
        private const val STORE_URL = "https://store.steampowered.com"

        private const val OWNED_GAMES_PATH = "/IPlayerService/GetOwnedGames/v1/"
        private const val NEWS_PATH = "/ISteamNews/GetNewsForApp/v2/"
        private const val WISHLIST_PATH = "/wishlist/profiles/"

        private val STEAM_ID_PATTERN = Pattern.compile("765\\d{14}")
        private val IMAGE_LINK_PATTERN =
            Pattern.compile("\\{STEAM.*((.jpg)|(.png)|(.gif))\\b|\\{STEAM.*")
    }

    suspend fun getOwnedGames(steamId: Long): List<Game> {
        val url = buildUrl(BASE_API_URL, OWNED_GAMES_PATH) {
            parametersOf(
                "skip_unvetted_apps" to "true",
                "key" to apiKey,
                "include_appinfo" to "true",
                "steamid" to steamId.toString()
            )
        }
        val response = fetch(url)

        return when {
            response == """{"response":{}}""" || response == """{"success": 2}""" ->
                throw IllegalStateException("Account is hidden or does not exist")

            else -> parseGames(response)
        }
    }

    suspend fun getNewsByOwnedGames(
        appId: String,
        newsCount: Int = 3,
        maxLength: Int = 300
    ): List<NewsItem> {
        val url = buildUrl(BASE_API_URL, NEWS_PATH) {
            parametersOf(
                "appid" to appId,
                "count" to newsCount,
                "maxlength" to maxLength
            )
        }
        return parseNewsItems(fetch(url))
    }

    suspend fun getWishlistGames(steamId: Long): List<Game> {
        val url = buildUrl(STORE_URL, "$WISHLIST_PATH$steamId/wishlistdata/")
        return try {
            parseWishlistGames(fetch(url))
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isValidSteamId(steamId: String): Boolean = STEAM_ID_PATTERN.matcher(steamId).matches()

    suspend fun checkWishlistAvailability(steamId: Long): Int {
        val url = buildUrl(STORE_URL, "$WISHLIST_PATH$steamId/wishlistdata/")
        return try {
            client.get(url).status.value
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> 500
                else -> 100
            }
        }
    }

    private suspend fun fetch(url: String): String = client.get(url) {
        header(HttpHeaders.UserAgent, USER_AGENT)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }.bodyAsText()

    private fun buildUrl(
        base: String,
        path: String,
        params: ParametersBuilder.() -> Unit = {}
    ): String =
        URLBuilder(base).apply {
            path(path)
            parameters.appendAll(ParametersBuilder().apply(params).build())
        }.buildString()

    private fun parseGames(json: String): List<Game> = objectMapper.readTree(json)
        .path("response")["games"]
        .let { gamesNode ->
            if (gamesNode.isArray) gamesNode.map { objectMapper.convertValue(it, Game::class.java) }
            else emptyList()
        }

    private fun parseWishlistGames(json: String): List<Game> = objectMapper.readTree(json)
        .let { rootNode ->
            if (rootNode.isObject) {
                rootNode.fields().asSequence().map { entry ->
                    Game(appid = entry.key, name = entry.value["name"].asText())
                }.toList()
            } else emptyList()
        }

    private fun parseNewsItems(json: String): List<NewsItem> = objectMapper.readTree(json)
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

    private fun isRecentNews(seconds: Long): Boolean {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val newsTime =
            LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault())
        // TODO CHECK THIS LINE!
        return newsTime.plus(30, ChronoUnit.MINUTES).isAfter(now)
        //return newsTime == now
    }

    private fun removeImageLinks(text: String): String =
        IMAGE_LINK_PATTERN.matcher(text).replaceAll("")
}