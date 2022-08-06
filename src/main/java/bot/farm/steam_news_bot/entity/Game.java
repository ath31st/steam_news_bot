package bot.farm.steam_news_bot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "games")
public class Game {
    @Id
    @Column(unique = true, nullable = false)
    private String appid;
    private String name;

    @ManyToMany(mappedBy = "games")
    private List<User> users;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(appid, game.appid) && Objects.equals(name, game.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appid, name);
    }
}
