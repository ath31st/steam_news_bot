package sidim.doma.infrastructure.plugin

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager

private const val DB_NAME = "steamidusers.db"
private const val JDBC_URL = "jdbc:sqlite:./$DB_NAME"
private const val EXISTING_SCHEMA_BASELINE_VERSION = "4"

fun configureDatabases() {
    val logger = LoggerFactory.getLogger("Database")

    runMigrations(logger)

    Database.connect(
        url = JDBC_URL,
        driver = "org.sqlite.JDBC",
    )
    logger.info("Database connected successfully")
}

private fun runMigrations(logger: Logger) {
    DriverManager.getConnection(JDBC_URL).use { connection ->
        val usersExists = tableExists(connection, "users")
        val flywayHistoryExists = tableExists(connection, "flyway_schema_history")

        val flyway = Flyway.configure()
            .dataSource(JDBC_URL, null, null)
            .locations("classpath:db/migration")
            .baselineVersion(EXISTING_SCHEMA_BASELINE_VERSION)
            .baselineDescription("Existing SchemaUtils schema")
            .load()

        if (usersExists && !flywayHistoryExists) {
            logger.info(
                "Existing database detected without Flyway history, baselining at V{}",
                EXISTING_SCHEMA_BASELINE_VERSION
            )
            flyway.baseline()
        }

        val result = flyway.migrate()
        logger.info("Flyway migrate completed: {} migrations applied", result.migrationsExecuted)
    }
}

private fun tableExists(connection: Connection, tableName: String): Boolean {
    connection.prepareStatement(
        "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?"
    ).use { statement ->
        statement.setString(1, tableName)
        statement.executeQuery().use { resultSet ->
            return resultSet.next()
        }
    }
}
