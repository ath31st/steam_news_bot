package sidim.doma.domain.game.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import sidim.doma.common.util.SortingOrder
import sidim.doma.domain.game.entity.Game
import sidim.doma.domain.game.entity.Games
import sidim.doma.domain.state.entity.UserGameStates
import sidim.doma.domain.user.entity.Users

class ExposedGameRepository : GameRepository {
    override fun createGames(games: List<Game>) {
        transaction {
            Games.batchInsert(games, ignore = true) { game ->
                this[Games.appid] = game.appid
                this[Games.name] = game.name
            }
        }
    }

    override fun createGame(game: Game): Game? {
        return transaction {
            Games.insert {
                it[appid] = game.appid
                it[name] = game.name
            }[Games.appid]
        }.let { findGameByAppId(it) }
    }

    override fun findGamesWithNullName(): List<Game> {
        return transaction {
            Games.selectAll()
                .where { Games.name.isNull() }
                .map { rowToGame(it) }
        }
    }

    override fun updateGames(games: List<Game>): Int {
        return transaction {
            val results = Games.batchUpsert(games) { game ->
                this[Games.appid] = game.appid
                this[Games.name] = game.name
            }
            results.size
        }
    }

    override fun findBannedGamesByChatId(
        chatId: String,
        pageNumber: Int,
        pageSize: Int,
        sortOrder: SortingOrder
    ): List<Game> {
        return transaction {
            val order = if (sortOrder == SortingOrder.ASC) SortOrder.ASC else SortOrder.DESC
            val offset: Long = ((pageNumber - 1) * pageSize).toLong()

            (Games innerJoin UserGameStates)
                .selectAll()
                .where { (UserGameStates.userId eq chatId) and (UserGameStates.isBanned eq true) }
                .orderBy(Games.name, order)
                .limit(pageSize)
                .offset(offset)
                .map { rowToGame(it) }
        }
    }

    override fun countBannedGamesByChatId(chatId: String): Long {
        return transaction {
            (Games innerJoin UserGameStates)
                .selectAll()
                .where { (UserGameStates.userId eq chatId) and (UserGameStates.isBanned eq true) }
                .count()
        }
    }

    override fun findByActiveUsersAndNotBanned(): List<Game> {
        return transaction {
            (Games innerJoin UserGameStates innerJoin Users)
                .select(Games.columns)
                .where { (Users.active eq true) and (UserGameStates.isBanned eq false) }
                .groupBy(Games.appid)
                .map { rowToGame(it) }
        }
    }

    override fun findGameByAppId(appId: String): Game? {
        return transaction {
            Games.selectAll()
                .where { Games.appid eq appId }
                .singleOrNull()
                ?.let { rowToGame(it) }
        }
    }

    override fun findGameByAppIds(appIds: Set<String>): List<Game> {
        return transaction {
            Games.selectAll()
                .where { Games.appid inList appIds }
                .map { rowToGame(it) }
        }
    }

    override fun countGames(): Long {
        return transaction {
            Games.selectAll().count()
        }
    }

    private fun rowToGame(it: ResultRow) = Game(
        appid = it[Games.appid],
        name = it[Games.name]
    )
}