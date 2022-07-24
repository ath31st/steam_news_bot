package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends CrudRepository<Game,String> {
}
