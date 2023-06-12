package bot.farm.steam_news_bot.controller;

import bot.farm.steam_news_bot.service.MetricsService;
import java.util.List;
import java.util.Map;
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
  public ResponseEntity<Map<String, Number>> getCountUsers() {
    return metricsService.getCountUsers();
  }
  
  @GetMapping("/users")
  public ResponseEntity<List<String>> getUsernames() {
    return metricsService.getAllUsers();
  }
  
  @GetMapping("/games")
  public ResponseEntity<Map<String, Long>> getCountGames() {
    return metricsService.getCountGames();
  }
  
  @GetMapping("/top-games")
  public ResponseEntity<List<String>> getTopGames() {
    return metricsService.getTopGames(10);
  }
}
