package bot.farm.steam_news_bot.entity;

import bot.farm.steam_news_bot.util.SecondsToLocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class NewsItem {

    @Id
    private long gid;
    private String title;
    private String url;
    private String author;
    private String contents;

    @JsonProperty("is_external_url")
    boolean isExternalUrl;

    @JsonProperty("feedlabel")
    private String feedLabel;
    @Convert(converter = SecondsToLocalDateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)
    private LocalDateTime date;

    @JsonProperty("feedname")
    private String feedName;

    @JsonProperty("feed_type")
    private int feedType;
    private String appid;

    @Override
    public String toString() {
        return date + "\n" + title + '\n' + contents + '\n' + url;
    }
}
