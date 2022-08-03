package bot.farm.steam_news_bot.controller;

import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/info")
public class InfoController {

    private final UserService userService;
    private final GameService gameService;

    public InfoController(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @GetMapping("/users")
    public ResponseEntity getCountUsers() {
        return ResponseEntity.ok(Map.of("total users in database", userService.getAllUsers().size(),
                "active users", userService.getUsersByActive(true).size()));
    }

    @GetMapping("/games")
    public ResponseEntity getCountGames() {
        return ResponseEntity.ok(Map.of("total games in database", gameService.countAllGames(),
                "games active users", gameService.countByUsersActive(true)));
    }
}
