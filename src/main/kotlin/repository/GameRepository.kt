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

    fun createGame(game: Game): Game? {
        return transaction {
            Games.insert {
                it[appid] = game.appid
                it[name] = game.name
            }[Games.appid]
        }.let { findGameByAppId(it) }
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
                .select(Games.columns)
                .where { (Users.active eq true) and (UserGameStates.isBanned eq false) }
                .groupBy(Games.appid)
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

    fun existsByAppId(appId: String): Boolean {
        return transaction {
            Games.selectAll().any { it[Games.appid] == appId }
        }
    }

    fun findGameByAppId(appId: String): Game? {
        return transaction {
            Games.selectAll()
                .where { Games.appid eq appId }
                .singleOrNull()
                ?.let { rowToGame(it) }
        }
    }

    private fun rowToGame(it: ResultRow) = Game(
        appid = it[Games.appid],
        name = it[Games.name]
    )
}