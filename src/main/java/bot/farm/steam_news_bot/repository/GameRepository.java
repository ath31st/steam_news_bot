package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface GameRepository extends CrudRepository<Game,String> {
    long countByStates_User_ActiveTrue();
 //   long countByUsers_Active(boolean active);

    Optional<Game> getGameByAppid(String appid);

    Optional<Game> findByName(String name);

//   Set<Game> findByUsers_ActiveTrue();
    Set<Game> findByStates_User_ActiveTrue();

    boolean existsByAppid(String appid);

}
