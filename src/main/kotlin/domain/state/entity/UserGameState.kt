package sidim.doma.domain.state.entity

import org.jetbrains.exposed.sql.Table
import sidim.doma.domain.game.entity.Games
import sidim.doma.domain.user.entity.Users

data class UserGameState(
    val userId: String,
    val gameId: String,
    val isWished: Boolean,
    val isBanned: Boolean,
    val isOwned: Boolean
)

object UserGameStates : Table("user_game_state") {
    val id = long("id").autoIncrement()
    val userId = varchar("user_id", 255).references(Users.chatId)
    val gameId = varchar("game_id", 255).references(Games.appid)
    val isWished = bool("is_wished")
    val isBanned = bool("is_banned")
    val isOwned = bool("is_owned")

    override val primaryKey = PrimaryKey(id)
}