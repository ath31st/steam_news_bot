package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserGameStateRepository extends JpaRepository<UserGameState, Long> {
  UserGameState findByUserAndGame(User user, Game game);
  
  UserGameState findByUser_ChatIdAndGame_Name(String chatId, String name);
  
  List<UserGameState> findByUser_ChatIdAndIsBannedTrue(String chatId);
  
  @Transactional
  @Modifying
  @Query("update UserGameState u set u.isBanned = ?1 where u.id = ?2")
  void updateIsBannedById(boolean isBanned, Long id);
  
  @Transactional
  @Modifying
  @Query("update UserGameState u set u.isWished = ?1, u.isOwned = ?2 where u.id = ?3")
  void updateIsWishedAndIsOwnedById(boolean isWished, boolean isOwned, Long id);
  
  @Transactional
  @Modifying
  @Query("delete from UserGameState u where u.user = ?1")
  void deleteByUser(User user);
  
  boolean existsByUserAndGame(User user, Game game);
  
  @Query(value = "SELECT g.name FROM user_game_state ugs "
      + "JOIN games g ON ugs.game_id = g.game_id "
      + "GROUP BY ugs.game_id, g.name "
      + "HAVING COUNT(ugs.game_id) > 5 "
      + "ORDER BY COUNT(ugs.game_id) "
      + "DESC LIMIT ?1", nativeQuery = true)
  List<String> findTopGames(int limit);
}
