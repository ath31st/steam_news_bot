package bot.farm.steam_news_bot.entity;

import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing the game state of a user.
 * Provides properties and methods to store and manipulate user game state data.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
public class UserGameState {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;
  
  @ManyToOne()
  @JoinColumn(name = "user_id")
  private User user;
  
  @ManyToOne(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
  @JoinColumn(name = "game_id")
  private Game game;
  
  private boolean isWished;
  private boolean isBanned;
  private boolean isOwned;
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserGameState that = (UserGameState) o;
    return Objects.equals(id, that.id)
        && Objects.equals(user, that.user) && Objects.equals(game, that.game);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(id, user, game);
  }
}
