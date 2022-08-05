package bot.farm.steam_news_bot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(unique = true, nullable = false)
    private String chatId;
    @NotNull
    private String name;
    @NotNull
    private Long steamId;
    private boolean active;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_game",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id"))
    private List<Game> games;

    @ManyToMany()
    @JoinTable(name = "users_black_list_games",
            joinColumns = @JoinColumn(name = "user_"),
            inverseJoinColumns = @JoinColumn(name = "black_list_games_id"))
    private List<BlackListGame> blackListGames = new ArrayList<>();

}
