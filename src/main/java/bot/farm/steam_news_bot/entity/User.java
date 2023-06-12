package bot.farm.steam_news_bot.entity;

import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
  @Id
  @Column(name = "user_id", unique = true, nullable = false)
  private String chatId;
  @NotNull
  private String name;
  @NotNull
  private Long steamId;
  private String locale;
  private boolean active;
  
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY,
      orphanRemoval = true)
  Set<UserGameState> states;
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(chatId, user.chatId) && Objects.equals(name, user.name);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(chatId, name);
  }
}
