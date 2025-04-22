package sidim.doma.application.statistics.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewsStatistics(
    val dailyCountNews: Int,
    val totalCountNews: Long,
)