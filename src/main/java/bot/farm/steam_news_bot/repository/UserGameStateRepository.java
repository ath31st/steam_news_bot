package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.UserGameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGameStateRepository extends JpaRepository<UserGameState, Long> {

}
