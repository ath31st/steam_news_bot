package sidim.doma.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.entity.UserGameState
import sidim.doma.entity.UserGameStates

class UserGameStateRepository {
    fun saveUserGameStates(ugs: List<UserGameState>) {
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

    fun saveUserGameState(
        ugs: UserGameState
    ) {
        return transaction {
            UserGameStates
                .insert {
                    it[userId] = ugs.userId
                    it[gameId] = ugs.gameId
                    it[isWished] = ugs.isWished
                    it[isBanned] = ugs.isBanned
                    it[isOwned] = ugs.isOwned
                }
        }
    }

    fun deleteUgsByUserId(userId: String) {
        return transaction {
            UserGameStates
                .deleteWhere { UserGameStates.userId eq userId }
        }
    }

    fun deleteUgsByGameIdAndUserId(gameId: String, userId: String) {
        return transaction {
            UserGameStates
                .deleteWhere {
                    (UserGameStates.userId eq userId) and (UserGameStates.gameId eq gameId)
                }
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

    fun updateIsBannedByGameIdAndUserId(banned: Boolean, gameId: String, userId: String) {
        transaction {
            UserGameStates.update({
                (UserGameStates.userId eq userId) and (UserGameStates.gameId eq gameId)
            }) {
                it[isBanned] = banned
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

    fun clearBlackListByUserId(userId: String) {
        return transaction {
            UserGameStates.update({
                (UserGameStates.userId eq userId) and (UserGameStates.isBanned eq true)
            }) {
                it[isBanned] = false
            }
        }
    }

    fun findByUserId(userId: String): List<UserGameState> {
        return transaction {
            UserGameStates
                .selectAll()
                .where { UserGameStates.userId eq userId }
                .map { rowToUserGameState(it) }
        }
    }

    fun updateIsWishedAndIsOwnedByGameIdAndUserId(
        isWished: Boolean,
        isOwned: Boolean,
        gameId: String,
        userId: String
    ) {
        transaction {
            UserGameStates.update({
                (UserGameStates.userId eq userId) and (UserGameStates.gameId eq gameId)
            }) {
                it[UserGameStates.isWished] = isWished
                it[UserGameStates.isOwned] = isOwned
            }
        }
    }

    private fun rowToUserGameState(it: ResultRow) = UserGameState(
        userId = it[UserGameStates.userId],
        gameId = it[UserGameStates.gameId],
        isWished = it[UserGameStates.isWished],
        isBanned = it[UserGameStates.isBanned],
        isOwned = it[UserGameStates.isOwned]
    )
}