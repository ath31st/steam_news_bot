package sidim.doma.application.statistics.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommonStatistics(
    val countUsers: Long,
    val countActiveUsers: Long,
    val countGames: Long,
)