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

/**
 * Repository interface for managing User entities.
 */
@Repository
public interface UserRepository extends CrudRepository<User, String> {
  /**
   * Checks if a user exists by chat ID.
   *
   * @param chatId The user's chat ID.
   * @return {@code true} if the user exists, {@code false} otherwise.
   */
  boolean existsByChatId(String chatId);

  /**
   * Finds a user by chat ID.
   *
   * @param chatId The user's chat ID.
   * @return The User entity.
   */
  User findByChatId(String chatId);

  /**
   * Counts the number of users with a specific chat ID and an owned game state.
   *
   * @param chatId The user's chat ID.
   * @return The count of users with the specified chat ID and owned game state.
   */
  long countByChatIdAndStates_IsOwnedTrue(String chatId);

  /**
   * Counts the number of users with a specific chat ID and wished game state.
   *
   * @param chatId The user's chat ID.
   * @return The count of users with the specified chat ID and wished game state.
   */
  long countByChatIdAndStates_IsWishedTrue(String chatId);

  /**
   * Counts the number of users with a specific active status.
   *
   * @param active The active status.
   * @return The count of users with the specified active status.
   */
  long countByActive(boolean active);

  /**
   * Finds a user by chat ID (optional).
   *
   * @param chatId The user's chat ID.
   * @return The optional User entity.
   */
  Optional<User> findUserByChatId(String chatId);

  /**
   * Finds a list of users by active status.
   *
   * @param active The active status.
   * @return The list of User entities.
   */
  List<User> findByActive(boolean active);

  /**
   * Finds a set of users with active status, a specific game app ID, and no banned game state.
   *
   * @param appid The game app ID.
   * @return The set of User entities.
   */
  Set<User> findByActiveTrueAndStates_Game_AppidAndStates_IsBannedFalse(String appid);

  /**
   * Checks if a user exists by chat ID, game name, and banned game state.
   *
   * @param chatId The user's chat ID.
   * @param name   The game name.
   * @return {@code true} if the user exists with the specified conditions, {@code false} otherwise.
   */
  boolean existsByChatIdAndStates_Game_NameAndStates_IsBannedTrue(String chatId, String name);

  /**
   * Updates the active status of a user by chat ID.
   *
   * @param active The new active status.
   * @param chatId The user's chat ID.
   */
  @Transactional
  @Modifying
  @Query("update User u set u.active = ?1 where u.chatId = ?2")
  void updateActiveByChatId(boolean active, String chatId);

  /**
   * Retrieves a list of usernames.
   *
   * @return The list of usernames.
   */
  @Query("select u.name from User u")
  List<String> getListUsername();

}
