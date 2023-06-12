package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
  boolean existsByChatId(String chatId);
  
  User findByChatId(String chatId);
  
  long countByChatIdAndStates_IsOwnedTrue(String chatId);
  
  long countByActive(boolean active);
  
  Optional<User> findUserByChatId(String chatId);
  
  List<User> findByActive(boolean active);
  
  Set<User> findByActiveTrueAndStates_Game_AppidAndStates_IsBannedFalse(String appid);
  
  boolean existsByChatIdAndStates_Game_NameAndStates_IsBannedTrue(String chatId, String name);
  
  @Transactional
  @Modifying
  @Query("update User u set u.active = ?1 where u.chatId = ?2")
  void updateActiveByChatId(boolean active, String chatId);
  
  @Query("select u.name from User u")
  List<String> getListUsername();
  
}
