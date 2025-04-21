package sidim.doma.infrastructure.integration.steam

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sidim.doma.infrastructure.integration.steam.dto.SteamAppDto
import sidim.doma.infrastructure.integration.steam.dto.SteamNewsItemDto
import sidim.doma.infrastructure.integration.steam.dto.SteamWishlistAppDto
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
        private const val OWNED_APPS_PATH = "/IPlayerService/GetOwnedGames/v1/"
        private const val GET_APPS_PATH = "/ICommunityService/GetApps/v1/"
        private const val NEWS_PATH = "/ISteamNews/GetNewsForApp/v2/"
        private const val WISHLIST_PATH = "/IWishlistService/GetWishlist/v1/"
        private const val APP_DETAILS_PATH = "$BASE_STORE_URL/api/appdetails"

        private val STEAM_ID_PATTERN = Pattern.compile("765\\d{14}")

        private val INVALID_RESPONSES = listOf(
            """{"response":{}}""",
            """{"success": 2}"""
        )
    }

    suspend fun getAppsByAppids(appIds: List<String>): List<SteamAppDto> {
        require(appIds.size <= 50)

        val queryParams = buildString {
            append("key=$apiKey")
            appIds.forEachIndexed { index, appId ->
                append("&appids[$index]=$appId")
            }
        }

        val response = fetch("$BASE_API_URL$GET_APPS_PATH?$queryParams")

        return when {
            response in INVALID_RESPONSES -> emptyList()
            else -> parseGetApps(response)
        }
    }

    suspend fun getOwnedApps(steamId: String): List<SteamAppDto> {
        val response = fetch("$BASE_API_URL$OWNED_APPS_PATH") {
            parameter("skip_unvetted_apps", "true")
            parameter("key", apiKey)
            parameter("include_appinfo", "true")
            parameter("steamid", steamId)
        }
        return when {
            response in INVALID_RESPONSES ->
                throw IllegalStateException("Account with steamId $steamId is hidden or does not exist")

            else -> parseOwnedApps(response)
        }
    }

    suspend fun getWishlistApps(steamId: String): List<SteamWishlistAppDto> {
        val response = fetch("$BASE_API_URL$WISHLIST_PATH") {
            parameter("steamid", steamId)
        }
        return when {
            response in INVALID_RESPONSES -> emptyList()

            else -> parseWishlistApps(response)
        }
    }

    suspend fun getNewsByAppid(
        appId: String,
        newsCount: Int,
        maxLength: Int
    ): List<SteamNewsItemDto> {
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

    private fun parseOwnedApps(json: String): List<SteamAppDto> {
        val gameNode = objectMapper.readTree(json).path("response")["games"]
        return when {
            gameNode == null || !gameNode.isArray -> emptyList()
            else -> gameNode.mapNotNull { objectMapper.convertValue<SteamAppDto>(it) }
        }
    }

    private fun parseGetApps(json: String): List<SteamAppDto> {
        val appsNode = objectMapper.readTree(json).path("response")["apps"]
        return when {
            appsNode == null || !appsNode.isArray -> emptyList()
            else -> appsNode.mapNotNull { appNode ->
                val appId = appNode.path("appid").asLong()
                val name = appNode.path("name").asText()
                val icon = appNode.path("icon").asText()
                val hasCommunityVisibleStats = appNode.path("community_visible_stats")
                    .takeIf { it.isBoolean }?.asBoolean()

                if (name.isNotEmpty()) SteamAppDto(
                    appid = appId,
                    name = name,
                    hashImgIconUrl = icon,
                    hasCommunityVisibleStats = hasCommunityVisibleStats
                ) else null
            }
        }
    }

    private fun parseWishlistApps(json: String): List<SteamWishlistAppDto> {
        val wishlistNode = objectMapper.readTree(json).path("response")["items"]
        return when {
            wishlistNode == null || !wishlistNode.isArray -> emptyList()
            else -> wishlistNode.mapNotNull { objectMapper.convertValue<SteamWishlistAppDto>(it) }
        }
    }

    private fun parseNewsItems(json: String): List<SteamNewsItemDto> {
        val newsNode = objectMapper.readTree(json).path("appnews")["newsitems"]
        return when {
            newsNode == null || !newsNode.isArray -> emptyList()
            else -> newsNode.mapNotNull { objectMapper.convertValue<SteamNewsItemDto>(it) }
        }
    }
}