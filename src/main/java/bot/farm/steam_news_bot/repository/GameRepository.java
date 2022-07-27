package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends CrudRepository<Game,String> {
    Optional<Game> getGameByAppid(String appid);

    List<Game> findByUsers_ActiveTrue();

    boolean existsByAppid(String appid);

}
