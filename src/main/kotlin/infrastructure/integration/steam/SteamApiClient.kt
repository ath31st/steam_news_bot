package sidim.doma.infrastructure.integration.steam

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import sidim.doma.infrastructure.integration.steam.dto.SteamAppDetailsDto
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

    suspend fun getAppDetails(appId: String): SteamAppDetailsDto? {
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

    private fun parseOwnedApps(json: String): List<SteamAppDto> = objectMapper.readTree(json)
        .path("response")["games"]
        .let { gamesNode ->
            if (gamesNode.isArray) gamesNode.map {
                objectMapper.convertValue(
                    it,
                    SteamAppDto::class.java
                )
            }
            else emptyList()
        }

    private fun parseGetApps(json: String): List<SteamAppDto> = objectMapper.readTree(json)
        .path("response")["apps"]
        .let { appsNode ->
            if (appsNode.isArray) appsNode.mapNotNull { dataNode ->
                val appId = dataNode.path("appid").asLong()
                val name = dataNode.path("name").asText()
                val icon = dataNode.path("icon").asText()
                if (name.isNotEmpty() && icon.isNotEmpty()) SteamAppDto(
                    appid = appId,
                    name = name,
                    hashImgIconUrl = icon,
                    hasCommunityVisibleStats = dataNode.path("community_visible_stats")
                        .takeIf { it.isBoolean }?.asBoolean()
                ) else null
            }
            else emptyList()
        }

    private fun parseAppDetails(json: String): SteamAppDetailsDto = objectMapper.readTree(json)
        .let { rootNode ->
            val appEntry = rootNode.fields().next()
            val dataNode = appEntry.value.path("data")

            require(!dataNode.isEmpty && dataNode.path("success").asBoolean(true))

            SteamAppDetailsDto(
                appid = appEntry.key,
                name = dataNode.path("name").asText(),
                type = dataNode.path("type").asText().takeIf { !it.isNullOrEmpty() },
                isFree = dataNode.path("is_free").asBoolean(false)
                    .takeIf { dataNode.path("is_free").isBoolean }
            )
        }

    private fun parseWishlistApps(json: String): List<SteamWishlistAppDto> =
        objectMapper.readTree(json)
            .path("response")["items"]
            .let { itemsNode ->
                if (itemsNode.isArray) itemsNode.map {
                    objectMapper.convertValue(
                        it,
                        SteamWishlistAppDto::class.java
                    )
                }
                else emptyList()
            }

    private fun parseNewsItems(json: String): List<SteamNewsItemDto> =
        objectMapper.readTree(json)
            .path("appnews")["newsitems"]
            .let { newsNode ->
                if (newsNode.isArray) {
                    newsNode.mapNotNull { objectMapper.convertValue<SteamNewsItemDto>(it) }
                } else emptyList()
            }
}