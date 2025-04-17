package sidim.doma.domain.game.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.common.dto.Page
import sidim.doma.domain.game.entity.Game
import sidim.doma.domain.game.entity.Games
import sidim.doma.domain.state.entity.UserGameStates
import sidim.doma.domain.user.entity.Users

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

    fun findGamesWithNullName(): List<Game> {
        return transaction {
            Games.selectAll()
                .where { Games.name.isNull() }
                .map { rowToGame(it) }
        }
    }

    fun updateGames(games: List<Game>): Int {
        return transaction {
            val results = Games.batchUpsert(games) { game ->
                this[Games.appid] = game.appid
                this[Games.name] = game.name
            }
            results.size
        }
    }

    fun getPageBannedByChatId(
        chatId: String,
        pageNumber: Int,
        pageSize: Int,
        sortOrder: SortOrder = SortOrder.ASC
    ): Page<Game> {
        return transaction {
            val offset: Long = ((pageNumber - 1) * pageSize).toLong()

            val query = (Games innerJoin UserGameStates).selectAll()
                .where { (UserGameStates.userId eq chatId) and (UserGameStates.isBanned eq true) }
                .orderBy(Games.name, sortOrder)

            val totalCount = query.count()

            val items = query.limit(count = pageSize)
                .offset(start = offset)
                .map { rowToGame(it) }

            val totalPages =
                (totalCount / pageSize).toInt() + if (totalCount % pageSize != 0L) 1 else 0

            Page(
                items = items,
                totalItems = totalCount,
                totalPages = totalPages,
                currentPage = pageNumber
            )
        }
    }

    fun getCountBannedByChatId(chatId: String): Long {
        return transaction {
            (Games innerJoin UserGameStates)
                .selectAll()
                .where {
                    (UserGameStates.userId eq chatId) and
                            (UserGameStates.isBanned eq true)
                }
                .count()
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

    fun findGameByAppId(appId: String): Game? {
        return transaction {
            Games.selectAll()
                .where { Games.appid eq appId }
                .singleOrNull()
                ?.let { rowToGame(it) }
        }
    }

    fun findGameByAppIds(appIds: Set<String>): List<Game> {
        return transaction {
            Games.selectAll()
                .where { Games.appid inList appIds }
                .map { rowToGame(it) }
        }
    }

    fun countGames(): Long {
        return transaction {
            Games.selectAll().count()
        }
    }

    private fun rowToGame(it: ResultRow) = Game(
        appid = it[Games.appid],
        name = it[Games.name]
    )
}