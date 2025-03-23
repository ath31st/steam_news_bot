package sidim.doma.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.exposed.sql.Table

data class NewsItem @JsonCreator constructor(
    @JsonProperty("gid") val gid: Long,
    @JsonProperty("title") val title: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("author") val author: String,
    @JsonProperty("contents") val contents: String,
    @JsonProperty("is_external_url") val isExternalUrl: Boolean,
    @JsonProperty("feedlabel") val feedLabel: String,
    @JsonProperty("feedname") val feedName: String,
    @JsonProperty("feed_type") val feedType: Int,
    @JsonProperty("appid") val appid: String,
    @JsonProperty("date") val date: String
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