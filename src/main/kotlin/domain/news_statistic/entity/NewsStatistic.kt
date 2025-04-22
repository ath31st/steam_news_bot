package sidim.doma.domain.news_statistic.entity

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

data class NewsStatistic(
    val recordDate: String,
    val dailyCount: Int
)

object NewsStatistics : Table("news_statistics") {
    val recordDate = date("date").uniqueIndex()
    val dailyCount = integer("daily_count").default(0)
    
    override val primaryKey = PrimaryKey(recordDate)
}