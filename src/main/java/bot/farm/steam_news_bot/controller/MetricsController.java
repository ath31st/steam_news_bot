package bot.farm.steam_news_bot.controller;

import bot.farm.steam_news_bot.service.MetricsService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller class for handling metrics API requests.
 * Provides endpoints related to retrieving metrics data.
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

  private final MetricsService metricsService;


  /**
   * Constructor for MetricsController.
   *
   * @param metricsService the MetricsService instance
   */
  public MetricsController(MetricsService metricsService) {
    this.metricsService = metricsService;
  }

  /**
   * Endpoint for retrieving the count of users.
   * Returns the count of users as a map with a single key-value pair.
   *
   * @return a ResponseEntity containing a map with the count of users
   */
  @GetMapping("/count-users")
  public ResponseEntity<Map<String, Number>> getCountUsers() {
    return metricsService.getCountUsers();
  }

  /**
   * Endpoint for retrieving the usernames of all users.
   * Returns a list of usernames.
   *
   * @return a ResponseEntity containing a list of usernames
   */
  @GetMapping("/users")
  public ResponseEntity<List<String>> getUsernames() {
    return metricsService.getAllUsers();
  }

  /**
   * Endpoint for retrieving the count of games.
   * Returns the count of games as a map with game names and corresponding counts.
   *
   * @return a ResponseEntity containing a map with the count of games
   */
  @GetMapping("/games")
  public ResponseEntity<Map<String, Long>> getCountGames() {
    return metricsService.getCountGames();
  }

  /**
   * Endpoint for retrieving the top N games.
   * Returns a list of the top N games based on certain criteria.
   *
   * @return a ResponseEntity containing a list of the top N games
   */
  @GetMapping("/top-games")
  public ResponseEntity<List<String>> getTopGames() {
    return metricsService.getTopGames(10);
  }
}
