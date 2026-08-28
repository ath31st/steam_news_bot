package sidim.doma.common.config

import java.io.File

object Env {
    private val fileValues: Map<String, String> = loadDotEnv()

    fun require(name: String): String =
        System.getenv(name)?.takeIf { it.isNotBlank() }
            ?: fileValues[name]?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(
                "$name is not set. Export it or add it to a .env file in the project root."
            )

    private fun loadDotEnv(): Map<String, String> {
        val file = File(".env")
        if (!file.isFile) return emptyMap()

        return file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val content = line.removePrefix("export ").trim()
                val separator = content.indexOf('=')
                if (separator <= 0) return@mapNotNull null
                val key = content.substring(0, separator).trim()
                val value = content.substring(separator + 1).trim().trimMatchingQuotes()
                key to value
            }
            .toMap()
    }

    private fun String.trimMatchingQuotes(): String =
        when {
            length >= 2 && first() == '"' && last() == '"' -> substring(1, lastIndex)
            length >= 2 && first() == '\'' && last() == '\'' -> substring(1, lastIndex)
            else -> this
        }
}
