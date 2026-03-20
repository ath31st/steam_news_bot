package sidim.doma.domain.news_statistics.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import sidim.doma.domain.news_statistics.entity.NewsStatistics
import sidim.doma.domain.news_statistics.entity.NewsStatistics.dailyCount
import sidim.doma.domain.news_statistics.entity.NewsStatistics.recordDate
import java.time.LocalDate

class ExposedNewsStatisticsRepository : NewsStatisticsRepository {
    override fun incrementDailyCount(date: LocalDate, count: Int) {
        transaction {
            NewsStatistics.upsert(
                where = { recordDate eq date },
                body = {
                    it[NewsStatistics.recordDate] = date
                    it[NewsStatistics.dailyCount] = count
                },
                onUpdate = {
                    it[dailyCount] = dailyCount + count
                }
            )
        }
    }

    override fun getDailyCount(date: LocalDate): Int {
        return transaction {
            NewsStatistics.selectAll()
                .where { recordDate eq date }
                .firstOrNull()
                ?.let { it[dailyCount] } ?: 0
        }
    }

    override fun getTotalCount(): Long = transaction {
        NewsStatistics.selectAll()
            .sumOf { it[dailyCount].toLong() }
    }
}