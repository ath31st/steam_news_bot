package bot.farm.steam_news_bot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

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
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "user_black_list_games",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "black_list_games_id"))
    private List<BlackListGame> blackListGames;

}
