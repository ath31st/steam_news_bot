package bot.farm.steam_news_bot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "game_id")
    private Game game;

    private boolean isWished;
    private boolean isBanned;
    private boolean isOwned;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGameState that = (UserGameState) o;
        return Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(game, that.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, game);
    }
}
