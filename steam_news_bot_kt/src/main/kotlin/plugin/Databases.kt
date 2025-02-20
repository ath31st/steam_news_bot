package sidim.doma.plugin

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:sqlite:./snb.db",
        user = "root",
        driver = "org.sqlite.JDBC",
        password = "",
    )
}
