package sidim.doma.domain.news.entity

import org.jetbrains.exposed.sql.Table

data class NewsItem(
    val gid: Long,
    val title: String,
    val url: String,
    val author: String,
    val contents: String,
    val appid: String,
    val date: Long
)

object NewsItems : Table("news_item") {
    val gid = long("gid").autoIncrement()
    val title = varchar("title", 255)
    val url = varchar("url", 255)
    val author = varchar("author", 255)
    val contents = varchar("contents", 255)
    val appid = varchar("appid", 255)
    val date = long("date")

    override val primaryKey = PrimaryKey(gid)
}