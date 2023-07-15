package bot.farm.steam_news_bot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a game.
 * Provides properties and methods to store and manipulate game data.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "games")
public class Game {
  @Id
  @Column(name = "game_id", unique = true, nullable = false)
  private String appid;
  private String name;

  @OneToMany(mappedBy = "game")
  Set<UserGameState> states;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Game game = (Game) o;
    return Objects.equals(appid, game.appid) && Objects.equals(name, game.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appid, name);
  }
}
