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

    @GetMapping("/users")
    public ResponseEntity getCountUsers() {
        return metricsService.getCountUsers();
    }

    @GetMapping("/games")
    public ResponseEntity getCountGames() {
       return metricsService.getCountGames();
    }
}
