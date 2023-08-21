package bot.farm.steam_news_bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Data class representing a message.
 * Provides properties and methods to store and manipulate message data.
 */
@Data
@Component
public class Message {
  @NotNull
  private String author;
  @NotNull
  private String text;
  @JsonIgnore
  private String date;

  @Override
  public String toString() {
    return new StringBuilder()
        .append("From/От ").append(author)
        .append(System.lineSeparator())
        .append("<b>").append(date).append("</b>")
        .append(System.lineSeparator())
        .append("Announcement!/Объявление! ").append(text)
        .append(System.lineSeparator())
        .append("Thank for your attention!/Благодарю за внимание!")
        .toString();
  }
}
