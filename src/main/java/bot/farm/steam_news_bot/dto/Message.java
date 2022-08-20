package bot.farm.steam_news_bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

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
                .append("From ").append(author)
                .append(System.lineSeparator())
                .append("<b>").append(date).append("</b>")
                .append(System.lineSeparator())
                .append("Announcement! ").append(text)
                .append(System.lineSeparator())
                .append("Thank you for your attention my friend!")
                .toString();
    }
}
