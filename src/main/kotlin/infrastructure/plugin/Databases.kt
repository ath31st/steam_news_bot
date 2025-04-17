package sidim.doma.infrastructure.plugin

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import sidim.doma.domain.game.entity.Games
import sidim.doma.domain.state.entity.UserGameStates
import sidim.doma.domain.user.entity.Users

fun configureDatabases() {
    val logger = LoggerFactory.getLogger("Database")

    try {
        val db = Database.connect(
            url = "jdbc:sqlite:./steamidusers.db",
            driver = "org.sqlite.JDBC",
        )
        logger.info("Database connected successfully")

        transaction(db) {
            SchemaUtils.create(Games, Users, UserGameStates)
            logger.info("Tables checked/created successfully")
        }
    } catch (e: Exception) {
        logger.error("Failed to connect to database: ${e.message}", e)
    }
}
