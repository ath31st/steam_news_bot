package sidim.doma.domain.news_statistic.service

import sidim.doma.domain.news_statistic.repository.NewsStatisticRepository
import java.time.LocalDate

class NewsStatisticService(private val newsStatisticRepository: NewsStatisticRepository) {
    fun incrementDailyCount(date: LocalDate, count: Int) =
        newsStatisticRepository.incrementDailyCount(date, count)

    fun getTotalCount() = newsStatisticRepository.getTotalCount()
}