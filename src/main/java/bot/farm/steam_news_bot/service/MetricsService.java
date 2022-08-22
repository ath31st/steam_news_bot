package bot.farm.steam_news_bot.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MetricsService {
    private final UserService userService;
    private final GameService gameService;

    public MetricsService(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public ResponseEntity getCountUsers() {
        return ResponseEntity.ok(Map.of("total users in database", userService.getAllUsers().size(),
                "active users", userService.countUsersByActive(true)));
    }
    public ResponseEntity getCountGames() {
        return ResponseEntity.ok(Map.of("total games in database", gameService.countAllGames(),
                "game states by active users", gameService.countByUsersActive()));
    }
}
