package sidim.doma.domain.news_statistics.service

import sidim.doma.domain.news_statistics.repository.NewsStatisticsRepository
import java.time.LocalDate

class NewsStatisticsService(private val newsStatisticsRepository: NewsStatisticsRepository) {
    fun incrementDailyCount(date: LocalDate, count: Int) =
        newsStatisticsRepository.incrementDailyCount(date, count)

    fun getDailyCount(date: LocalDate) = newsStatisticsRepository.getDailyCount(date)
    fun getTotalCount() = newsStatisticsRepository.getTotalCount()
}