package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findUserByChatId(String chatId);

    List<User> findByActive(boolean active);

  //  List<User> findByGames_AppidAndActiveTrue(String appid);

    Set<User> findByActiveTrueAndStates_Game_AppidAndStates_IsBannedFalse(String appid);

    @Transactional
    @Modifying
    @Query("update User u set u.active = ?1 where u.chatId = ?2")
    void updateActiveByChatId(boolean active, String chatId);

}
