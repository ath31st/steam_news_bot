package bot.farm.snb.repository;

import bot.farm.snb.entity.Game;
import bot.farm.snb.entity.User;
import bot.farm.snb.entity.UserGameState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository interface for managing UserGameState entities.
 */
@Repository
public interface UserGameStateRepository extends JpaRepository<UserGameState, Long> {
  /**
   * Finds a UserGameState by user and game.
   *
   * @param user The User entity.
   * @param game The Game entity.
   * @return The UserGameState entity.
   */
  UserGameState findByUserAndGame(User user, Game game);

  /**
   * Finds a UserGameState by user's chat ID and game's name.
   *
   * @param chatId The user's chat ID.
   * @param name   The game's name.
   * @return The UserGameState entity.
   */

  UserGameState findByUser_ChatIdAndGame_Name(String chatId, String name);

  /**
   * Finds a list of UserGameStates by user's chat ID and isBanned is true.
   *
   * @param chatId The user's chat ID.
   * @return The list of UserGameState entities.
   */
  List<UserGameState> findByUser_ChatIdAndIsBannedTrue(String chatId);

  /**
   * Updates the isBanned field of a UserGameState by ID.
   *
   * @param isBanned The new value for isBanned.
   * @param id       The ID of the UserGameState entity.
   */
  @Transactional
  @Modifying
  @Query("update UserGameState u set u.isBanned = ?1 where u.id = ?2")
  void updateIsBannedById(boolean isBanned, Long id);

  /**
   * Updates the isWished and isOwned fields of a UserGameState by ID.
   *
   * @param isWished The new value for isWished.
   * @param isOwned  The new value for isOwned.
   * @param id       The ID of the UserGameState entity.
   */
  @Transactional
  @Modifying
  @Query("update UserGameState u set u.isWished = ?1, u.isOwned = ?2 where u.id = ?3")
  void updateIsWishedAndIsOwnedById(boolean isWished, boolean isOwned, Long id);

  /**
   * Deletes UserGameStates associated with a user.
   *
   * @param user The User entity.
   */
  @Transactional
  @Modifying
  @Query("delete from UserGameState u where u.user = ?1")
  void deleteByUser(User user);

  /**
   * Checks if a UserGameState exists for a user and game.
   *
   * @param user The User entity.
   * @param game The Game entity.
   * @return {@code true} if the UserGameState exists, {@code false} otherwise.
   */
  boolean existsByUserAndGame(User user, Game game);

  /**
   * Finds the top games based on the count of UserGameStates.
   *
   * @param limit The maximum number of games to retrieve.
   * @return The list of game names.
   */
  @Query(value = "SELECT g.name FROM user_game_state ugs "
      + "JOIN games g ON ugs.game_id = g.game_id "
      + "GROUP BY ugs.game_id, g.name "
      + "HAVING COUNT(ugs.game_id) > 5 "
      + "ORDER BY COUNT(ugs.game_id) "
      + "DESC LIMIT ?1", nativeQuery = true)
  List<String> findTopGames(int limit);
}
