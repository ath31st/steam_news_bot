package sidim.doma.domain.user.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.domain.state.entity.UserGameStates
import sidim.doma.domain.user.entity.User
import sidim.doma.domain.user.entity.Users

class ExposedUserRepository : UserRepository {
    override fun existsByChatId(chatId: String): Boolean {
        return transaction {
            Users.selectAll()
                .where(Users.chatId eq chatId)
                .singleOrNull()
                ?.let { true } ?: false
        }
    }

    override fun findByChatId(chatId: String): User? {
        return transaction {
            Users.selectAll()
                .where(Users.chatId eq chatId)
                .singleOrNull()
                ?.let { rowToUser(it) }
        }
    }

    override fun findAll(): List<User> {
        return transaction {
            Users.selectAll()
                .map { rowToUser(it) }
        }
    }

    override fun findAllByActive(isActive: Boolean): List<User> {
        return transaction {
            Users.selectAll()
                .where(Users.active eq isActive)
                .map { rowToUser(it) }
        }
    }

    override fun findByActiveAndAppidAndBanned(
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

    override fun create(user: User): User? {
        transaction {
            Users.insert {
                it[chatId] = user.chatId
                it[name] = user.name
                it[steamId] = user.steamId
                it[locale] = user.locale
                it[active] = user.active
            }[Users.chatId]
        }
        return findByChatId(user.chatId)
    }

    override fun update(chatId: String, name: String?, steamId: Long, locale: String): User? {
        transaction {
            Users.update({ Users.chatId eq chatId }) {
                if (name != null) it[Users.name] = name
                it[Users.steamId] = steamId
                it[Users.locale] = locale
            }
        }
        return findByChatId(chatId)
    }

    override fun updateActiveByChatId(isActive: Boolean, chatId: String): Int {
        return transaction {
            Users.update({ Users.chatId eq chatId }) {
                it[active] = isActive
            }
        }
    }

    override fun countUsers(): Long {
        return transaction {
            Users.selectAll().count()
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