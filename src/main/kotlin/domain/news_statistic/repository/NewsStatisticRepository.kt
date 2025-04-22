package sidim.doma.domain.news_statistic.repository

import java.time.LocalDate

interface NewsStatisticRepository {
    fun incrementDailyCount(date: LocalDate, count: Int)
    fun getDailyCount(date: LocalDate): Int
    fun getTotalCount(): Long
}