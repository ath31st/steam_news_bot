package bot.farm.steam_news_bot.controller;

import bot.farm.steam_news_bot.service.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/count-users")
    public ResponseEntity getCountUsers() {
        return metricsService.getCountUsers();
    }

    @GetMapping("/users")
    public ResponseEntity getUsernames() {
        return metricsService.getAllUsers();
    }

    @GetMapping("/games")
    public ResponseEntity getCountGames() {
        return metricsService.getCountGames();
    }

    @GetMapping("/top-games")
    public ResponseEntity getTopGames() {
        return metricsService.getTopGames(10);
    }
}
