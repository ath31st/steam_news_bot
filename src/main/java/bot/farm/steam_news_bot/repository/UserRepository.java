package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User,String> {
    Optional<User> findUserByChatId(String chatId);
}
