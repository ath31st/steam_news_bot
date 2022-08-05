package bot.farm.steam_news_bot.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "black_list")
public class BlackListGame {

    @Id
    @Column(unique = true, nullable = false)
    private String appid;

    private String chatId;


    private String name;

    @ManyToMany(mappedBy = "blackListGames")
    private List<User> users = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlackListGame that = (BlackListGame) o;
        return Objects.equals(chatId, that.chatId) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, name);
    }
}
