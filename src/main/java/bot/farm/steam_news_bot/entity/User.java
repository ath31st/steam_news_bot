package bot.farm.steam_news_bot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class User {
    @Id
    private String chatId;
    @NotNull
    private String name;
    @NotNull
    private Long steamId;
}
