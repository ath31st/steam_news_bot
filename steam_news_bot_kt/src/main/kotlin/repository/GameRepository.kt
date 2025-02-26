package sidim.doma.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.entity.Game
import sidim.doma.entity.Games
import sidim.doma.entity.UserGameStates
import sidim.doma.entity.Users

class GameRepository {
    fun createGames(games: List<Game>) {
        transaction {
            Games.batchInsert(games, ignore = true) { game ->
                this[Games.appid] = game.appid
                this[Games.name] = game.name
            }
        }
    }

    fun findBannedByChatId(chatId: String): List<Game> {
        return transaction {
            (Games innerJoin UserGameStates)
                .selectAll()
                .where {
                    (UserGameStates.userId eq chatId) and
                            (UserGameStates.isBanned eq true)
                }
                .map { rowToGame(it) }
        }
    }

    fun findByActiveUsersAndNotBanned(): List<Game> {
        return transaction {
            (Games innerJoin UserGameStates innerJoin Users)
                .selectAll()
                .where {
                    (Users.active eq true) and
                            (UserGameStates.isBanned eq false)
                }
                .distinct()
                .map { rowToGame(it) }
        }
    }

    fun findTopGames(limit: Long): List<Game> {
        return transaction {
            (Games innerJoin UserGameStates)
                .selectAll()
                .where {
                    (UserGameStates.isBanned eq false)
                }
                .groupBy(Games.name)
                .having { UserGameStates.gameId.count() greater limit }
                .orderBy(UserGameStates.gameId.count() to SortOrder.DESC)
                .map { rowToGame(it) }
        }
    }

    fun countAllGames(): Long {
        return transaction {
            Games.selectAll().count()
        }
    }

    fun countGamesByUsersIsActive(isActive: Boolean): Long {
        return transaction {
            (Games innerJoin UserGameStates innerJoin Users)
                .selectAll()
                .where {
                    (Users.active eq isActive)
                }
                .distinct()
                .count()
                .toLong()
        }
    }

    private fun rowToGame(it: ResultRow) = Game(
        appid = it[Games.appid],
        name = it[Games.name]
    )
}