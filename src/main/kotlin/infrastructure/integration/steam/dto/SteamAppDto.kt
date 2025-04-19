package sidim.doma.infrastructure.integration.steam.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SteamAppDto @JsonCreator constructor(
    @JsonProperty("appid") val appid: Long,
    @JsonProperty("name") val name: String,
    @JsonProperty("img_icon_url") val hashImgIconUrl: String,
    @JsonProperty("playtime_forever") val playtimeForever: Int? = null,
    @JsonProperty("has_community_visible_stats") val hasCommunityVisibleStats: Boolean? = null,
    @JsonProperty("rtime_last_played") val recentTimeLastPlayed: Long? = null,
)