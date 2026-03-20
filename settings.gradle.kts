rootProject.name = "steam_news_bot_kt"

pluginManagement {
    val kotlinVersion: String by settings
    val ktorVersion: String by settings
    val flywayVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("io.ktor.plugin") version ktorVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("org.flywaydb.flyway") version flywayVersion
    }
}