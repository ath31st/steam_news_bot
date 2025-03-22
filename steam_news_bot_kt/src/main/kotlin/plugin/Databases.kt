package sidim.doma.plugin

import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

fun configureDatabases() {
    val logger = LoggerFactory.getLogger("Database")

    try {
        Database.connect(
            url = "jdbc:sqlite:./steamidusers.db",
            driver = "org.sqlite.JDBC",
        )
        logger.info("Database connected successfully")
    } catch (e: Exception) {
        logger.error("Failed to connect to database: ${e.message}", e)
    }
}
