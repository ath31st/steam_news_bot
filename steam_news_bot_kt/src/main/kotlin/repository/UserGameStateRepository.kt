package sidim.doma.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.entity.Games
import sidim.doma.entity.UserGameState
import sidim.doma.entity.UserGameStates

class UserGameStateRepository {
    fun createUgs(ugs: List<UserGameState>) {
        return transaction {
            UserGameStates.batchInsert(ugs) {
                this[UserGameStates.userId] = it.userId
                this[UserGameStates.gameId] = it.gameId
                this[UserGameStates.isWished] = it.isWished
                this[UserGameStates.isBanned] = it.isBanned
                this[UserGameStates.isOwned] = it.isOwned
            }
        }
    }

    fun deleteUgsByUserId(userId: String) {
        return transaction {
            UserGameStates
                .deleteWhere { UserGameStates.userId eq userId }
        }
    }

    fun findByUserIdAndGameId(userId: String, gameId: String): UserGameState? {
        return transaction {
            UserGameStates
                .selectAll()
                .where {
                    (UserGameStates.userId eq userId) and
                            (UserGameStates.gameId eq gameId)
                }
                .singleOrNull()
                ?.let { rowToUserGameState(it) }
        }
    }

    fun findByUserIdAndGameName(userId: String, gameName: String): UserGameState? {
        return transaction {
            (UserGameStates innerJoin Games)
                .selectAll()
                .where {
                    (UserGameStates.userId eq userId) and
                            (Games.name eq gameName)
                }
                .singleOrNull()
                ?.let { rowToUserGameState(it) }
        }
    }

    fun findByUserIdAndBanned(userId: String, isBanned: Boolean): UserGameState? {
        return transaction {
            UserGameStates
                .selectAll()
                .where {
                    (UserGameStates.userId eq userId) and
                            (UserGameStates.isBanned eq isBanned)
                }
                .singleOrNull()
                ?.let { rowToUserGameState(it) }
        }
    }

    fun updateIsBannedById(isBanned: Boolean, id: Long) {
        transaction {
            UserGameStates.update({ UserGameStates.id eq id }) {
                it[UserGameStates.isBanned] = isBanned
            }
        }
    }

    fun updateIsBannedByGameNameAndUserId(banned: Boolean, gameName: String, userId: String) {
        transaction {
            (UserGameStates innerJoin Games)
                .update({ (UserGameStates.userId eq userId) and (Games.name eq gameName) }) {
                    it[UserGameStates.isBanned] = banned
                }
        }
    }

    fun updateIsWishedAndIsOwnedById(isWished: Boolean, isOwned: Boolean, id: Long) {
        transaction {
            UserGameStates.update({ UserGameStates.id eq id }) {
                it[UserGameStates.isWished] = isWished
                it[UserGameStates.isOwned] = isOwned
            }
        }
    }

    fun existsByUserIdAndGameId(userId: String, gameId: String): Boolean {
        return transaction {
            UserGameStates
                .selectAll()
                .where {
                    (UserGameStates.userId eq userId) and
                            (UserGameStates.gameId eq gameId)
                }
                .singleOrNull()
                ?.let { true } ?: false
        }
    }

    fun countByUserIdAndIsOwned(userId: String, isOwned: Boolean): Long {
        return transaction {
            UserGameStates
                .selectAll()
                .where {
                    (UserGameStates.userId eq userId) and
                            (UserGameStates.isOwned eq isOwned)
                }
                .count()
        }
    }

    fun countByUserIdAndIsWished(userId: String, isWished: Boolean): Long {
        return transaction {
            UserGameStates
                .selectAll()
                .where {
                    (UserGameStates.userId eq userId) and
                            (UserGameStates.isWished eq isWished)
                }
                .count()
        }
    }

    private fun rowToUserGameState(it: ResultRow) = UserGameState(
        userId = it[UserGameStates.userId],
        gameId = it[UserGameStates.gameId],
        isWished = it[UserGameStates.isWished],
        isBanned = it[UserGameStates.isBanned],
        isOwned = it[UserGameStates.isOwned]
    )

    fun clearBlackListByUserId(userId: String) {
        return transaction {
            UserGameStates.update({
                (UserGameStates.userId eq userId) and (UserGameStates.isBanned eq true)
            }) {
                it[isBanned] = false
            }
        }
    }
}