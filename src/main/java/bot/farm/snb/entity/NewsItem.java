package bot.farm.snb.entity;

import bot.farm.snb.util.CustomDataDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a news item.
 * Provides properties and methods to store and manipulate news item data.
 */
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
  @JsonDeserialize(using = CustomDataDeserializer.class)
  private String date;

  @JsonProperty("feedname")
  private String feedName;

  @JsonProperty("feed_type")
  private int feedType;
  private String appid;

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    return stringBuilder
        .append("<b>").append(date).append("</b>") // bold it
        .append(System.lineSeparator())
        .append("<b>").append(title).append("</b>")
        .append(System.lineSeparator())
        .append(contents)
        .append(System.lineSeparator())
        .append("<a href=\"").append(url).append("\">").append("LINK").append("(").append(appid).append(")").append("</a>")
        .toString();
  }
}
