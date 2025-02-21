package sidim.doma.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.entity.User
import sidim.doma.entity.UserGameStates
import sidim.doma.entity.Users

class UserRepository {

    fun existsByChatId(chatId: String): Boolean {
        return transaction {
            Users.selectAll()
                .where(Users.chatId eq chatId)
                .singleOrNull()
                ?.let { true } ?: false
        }
    }

    fun findByChatId(chatId: String): User? {
        return transaction {
            Users.selectAll()
                .where(Users.chatId eq chatId)
                .singleOrNull()
                ?.let { rowToUser(it) }
        }
    }

    fun findAll(): List<User> {
        return transaction {
            Users.selectAll()
                .map { rowToUser(it) }
        }
    }

    fun findAllByActive(isActive: Boolean): List<User> {
        return transaction {
            Users.selectAll()
                .where(Users.active eq isActive)
                .map { rowToUser(it) }
        }
    }

    fun findByActiveAndAppidAndBanned(
        isActive: Boolean,
        appId: String,
        isBanned: Boolean
    ): List<User> {
        return transaction {
            (Users innerJoin UserGameStates)
                .selectAll()
                .where {
                    (Users.active eq isActive) and
                            (UserGameStates.gameId eq appId) and
                            (UserGameStates.isBanned eq isBanned)
                }
                .map { rowToUser(it) }
        }
    }

    fun create(user: User): String {
        return transaction {
            Users.insert {
                it[chatId] = user.chatId
                it[name] = user.name
                it[steamId] = user.steamId
                it[locale] = user.locale
                it[active] = user.active
            }[Users.chatId]
        }
    }

    fun update(user: User) {
        transaction {
            Users.update({ Users.chatId eq user.chatId }) {
                it[name] = user.name
                it[steamId] = user.steamId
                it[locale] = user.locale
                it[active] = user.active
            }
        }
    }

    fun updateActiveByChatId(isActive: Boolean, chatId: String) {
        transaction {
            Users.update({ Users.chatId eq chatId }) {
                it[active] = isActive
            }
        }
    }

    private fun rowToUser(it: ResultRow) = User(
        chatId = it[Users.chatId],
        name = it[Users.name],
        steamId = it[Users.steamId],
        locale = it[Users.locale],
        active = it[Users.active]
    )
}