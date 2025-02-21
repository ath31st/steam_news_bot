package sidim.doma.entity

import org.jetbrains.exposed.sql.Table

data class Game(
    val appid: String,
    val name: String
)

object Games : Table("Games") {
    val appid = varchar("game_id", 255)
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(appid)
}
