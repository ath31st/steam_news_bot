package sidim.doma.entity

import org.jetbrains.exposed.sql.Table

data class NewsItem(
    val gid: Long,
    val title: String,
    val url: String,
    val author: String,
    var contents: String,
    val isExternalUrl: Boolean,
    val feedLabel: String,
    val feedName: String,
    val feedType: Int,
    val appid: String,
    val date: String
)

object NewsItems : Table("news_item") {
    val gid = long("gid").autoIncrement()
    val title = varchar("title", 255)
    val url = varchar("url", 255)
    val author = varchar("author", 255)
    val contents = varchar("contents", 255)
    val isExternalUrl = bool("is_external_url")
    val feedLabel = varchar("feed_label", 255)
    val feedName = varchar("feed_name", 255)
    val feedType = integer("feed_type")
    val appid = varchar("appid", 255)
    val date = varchar("date", 255)

    override val primaryKey = PrimaryKey(gid)
}