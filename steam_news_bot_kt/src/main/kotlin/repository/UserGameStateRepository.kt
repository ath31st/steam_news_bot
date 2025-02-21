package sidim.doma.repository

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import sidim.doma.entity.Games
import sidim.doma.entity.UserGameState
import sidim.doma.entity.UserGameStates

class UserGameStateRepository {
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

    private fun rowToUserGameState(it: ResultRow) = UserGameState(
        userId = it[UserGameStates.userId],
        gameId = it[UserGameStates.gameId],
        isWished = it[UserGameStates.isWished],
        isBanned = it[UserGameStates.isBanned],
        isOwned = it[UserGameStates.isOwned]
    )
}