package bot.farm.steam_news_bot.service;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {
  private final UserService userService;
  private final GameService gameService;
  private final UserGameStateService userGameStateService;
  
  public MetricsService(UserService userService, GameService gameService, UserGameStateService userGameStateService) {
    this.userService = userService;
    this.gameService = gameService;
    this.userGameStateService = userGameStateService;
  }
  
  public ResponseEntity getCountUsers() {
    return ResponseEntity.ok(Map.of("total users in database", userService.getAllUsers().size(),
        "active users", userService.countUsersByActive(true)));
  }
  
  public ResponseEntity getCountGames() {
    return ResponseEntity.ok(Map.of("total games in database", gameService.countAllGames(),
        "game states by active users", gameService.countByUsersActive()));
  }
  
  public ResponseEntity getAllUsers() {
    return ResponseEntity.ok(userService.getListUsername());
  }
  
  public ResponseEntity getTopGames(int limit) {
    return ResponseEntity.ok(userGameStateService.getTopGamesFromDb(limit));
  }
}
