package sidim.doma.infrastructure.integration.steam.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SteamWishlistAppDto @JsonCreator constructor(
    @JsonProperty("appid") val appid: Long,
    @JsonProperty("priority") val priority: Int,
    @JsonProperty("date_added") val dateAdded: Long,
)