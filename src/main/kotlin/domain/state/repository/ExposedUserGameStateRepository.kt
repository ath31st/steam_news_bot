package sidim.doma.domain.state.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.domain.state.entity.UserGameState
import sidim.doma.domain.state.entity.UserGameStates

class ExposedUserGameStateRepository : UserGameStateRepository {
    override fun saveUserGameStates(ugs: List<UserGameState>) {
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

    override fun saveUserGameState(
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

    override fun deleteUgsByUserId(userId: String) {
        return transaction {
            UserGameStates
                .deleteWhere { UserGameStates.userId eq userId }
        }
    }

    override fun deleteUgsByGameIdAndUserId(gameId: String, userId: String) {
        return transaction {
            UserGameStates
                .deleteWhere {
                    (UserGameStates.userId eq userId) and (UserGameStates.gameId eq gameId)
                }
        }
    }

    override fun findByUserIdAndGameId(userId: String, gameId: String): UserGameState? {
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

    override fun updateIsBannedByGameIdAndUserId(banned: Boolean, gameId: String, userId: String) {
        transaction {
            UserGameStates.update({
                (UserGameStates.userId eq userId) and (UserGameStates.gameId eq gameId)
            }) {
                it[isBanned] = banned
            }
        }
    }

    override fun existsByUserIdAndGameId(userId: String, gameId: String): Boolean {
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

    override fun countByUserIdAndIsOwned(userId: String, isOwned: Boolean): Long {
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

    override fun countByUserIdAndIsWished(userId: String, isWished: Boolean): Long {
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

    override fun clearBlackListByUserId(userId: String) {
        return transaction {
            UserGameStates.update({
                (UserGameStates.userId eq userId) and (UserGameStates.isBanned eq true)
            }) {
                it[isBanned] = false
            }
        }
    }

    override fun findByUserId(userId: String): List<UserGameState> {
        return transaction {
            UserGameStates
                .selectAll()
                .where { UserGameStates.userId eq userId }
                .map { rowToUserGameState(it) }
        }
    }

    override fun updateIsWishedAndIsOwnedByGameIdAndUserId(
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

    override fun findWishlistStatesByUserAndGameIds(userIdAppIdPairs: List<Pair<String, String>>): List<UserGameState> {
        return transaction {
            val userIds = userIdAppIdPairs.map { it.first }.distinct()
            val gameIds = userIdAppIdPairs.map { it.second }.distinct()

            UserGameStates
                .selectAll().where {
                    (UserGameStates.userId inList userIds) and (UserGameStates.gameId inList gameIds)
                }
                .map { rowToUserGameState(it) }
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