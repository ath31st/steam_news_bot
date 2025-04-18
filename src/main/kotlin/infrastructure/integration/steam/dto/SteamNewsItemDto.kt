package sidim.doma.infrastructure.integration.steam.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SteamNewsItemDto @JsonCreator constructor(
    @JsonProperty("gid") val gid: Long,
    @JsonProperty("title") val title: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("author") val author: String,
    @JsonProperty("contents") val contents: String,
    @JsonProperty("is_external_url") val isExternalUrl: Boolean,
    @JsonProperty("feedlabel") val feedLabel: String,
    @JsonProperty("feedname") val feedName: String,
    @JsonProperty("feed_type") val feedType: Int,
    @JsonProperty("appid") val appid: String,
    @JsonProperty("date") val date: Long
)