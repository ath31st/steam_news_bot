package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface GameRepository extends CrudRepository<Game,String> {
    Optional<Game> getGameByAppid(String appid);

    Set<Game> findByUsers_ActiveTrue();

    boolean existsByAppid(String appid);

}
