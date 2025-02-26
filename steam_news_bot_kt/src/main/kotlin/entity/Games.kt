package sidim.doma.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.sql.Table

data class Game @JsonCreator constructor(
    @JsonProperty("appid") val appid: String,
    @JsonProperty("name") val name: String?
)

object Games : Table("Games") {
    val appid = varchar("game_id", 255)
    val name = varchar("name", 255).nullable()

    override val primaryKey = PrimaryKey(appid)
}
