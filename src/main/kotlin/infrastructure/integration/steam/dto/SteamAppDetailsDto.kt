package sidim.doma.infrastructure.integration.steam.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SteamAppDetailsDto @JsonCreator constructor(
    @JsonProperty("appid") val appid: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("type") val type: String? = null,
    @JsonProperty("is_free") val isFree: Boolean? = null
)