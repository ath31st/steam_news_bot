package sidim.doma.entity

import org.jetbrains.exposed.sql.Table

data class User(
    val chatId: String,
    val name: String,
    val steamId: Long,
    val locale: String,
    val active: Boolean,
)

object Users : Table("Users") {
    val chatId = varchar("user_id", 255)
    val name = varchar("name", 255)
    val steamId = long("steam_id")
    val locale = varchar("locale", 255)
    val active = bool("active")

    override val primaryKey = PrimaryKey(chatId)
}