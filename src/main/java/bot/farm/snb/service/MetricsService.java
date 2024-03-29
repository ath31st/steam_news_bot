package bot.farm.snb.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service class for providing metrics related to users and games.
 */
@Service
@RequiredArgsConstructor
public class MetricsService {
  private final UserService userService;
  private final GameService gameService;
  private final UserGameStateService userGameStateService;

  /**
   * Retrieves the count of users in the database, including total users and active users.
   *
   * @return A ResponseEntity containing a map with the count of total users and active users.
   */
  public ResponseEntity<Map<String, Number>> getCountUsers() {
    return ResponseEntity.ok(Map.of("total users in database", userService.getAllUsers().size(),
        "active users", userService.countUsersByActive(true)));
  }

  /**
   * Retrieves the count of games in the database, including total games and game states by
   * active users.
   *
   * @return A ResponseEntity containing a map with the count of total games and game states
   *     by active users.
   */
  public ResponseEntity<Map<String, Long>> getCountGames() {
    return ResponseEntity.ok(Map.of("total games in database", gameService.countAllGames(),
        "game states by active users", gameService.countByUsersActive()));
  }

  /**
   * Retrieves the list of usernames for all users in the database.
   *
   * @return A ResponseEntity containing a list of usernames.
   */
  public ResponseEntity<List<String>> getAllUsers() {
    return ResponseEntity.ok(userService.getListUsername());
  }

  /**
   * Retrieves the top games based on the number of occurrences in the user game state database.
   *
   * @param limit The maximum number of top games to retrieve.
   * @return A ResponseEntity containing a list of top game names.
   */
  public ResponseEntity<List<String>> getTopGames(int limit) {
    return ResponseEntity.ok(userGameStateService.getTopGamesFromDb(limit));
  }
}
