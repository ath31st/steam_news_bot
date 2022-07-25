package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends CrudRepository<User,String> {
    Optional<User> findUserByChatId(String chatId);

    List<User> findByGamesAppids_Appid(String appid);




}
