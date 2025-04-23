package sidim.doma.domain.news_statistics.repository

import java.time.LocalDate

interface NewsStatisticsRepository {
    fun incrementDailyCount(date: LocalDate, count: Int)
    fun getDailyCount(date: LocalDate): Int
    fun getTotalCount(): Long
}