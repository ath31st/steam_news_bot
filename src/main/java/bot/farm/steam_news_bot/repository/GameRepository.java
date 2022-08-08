package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends CrudRepository<Game,String> {
    long countByStates_User_ActiveTrue();

    List<Game> findByStates_User_ChatIdAndStates_IsBannedTrue(String chatId);

    List<Game> findByStates_User_ActiveTrue();

    List<Game> findByStates_User_ActiveTrueAndStates_IsBannedFalse();

    boolean existsByAppid(String appid);

}
