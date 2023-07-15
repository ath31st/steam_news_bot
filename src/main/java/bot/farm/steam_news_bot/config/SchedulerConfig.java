package bot.farm.steam_news_bot.config;

import bot.farm.steam_news_bot.SteamNewsBot;
import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.NewsItem;
import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.SteamService;
import bot.farm.steam_news_bot.service.UserService;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration class for scheduling tasks related to Steam news.
 * Enables scheduling and specifies conditional property for activation.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {

  private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
  private final SteamNewsBot steamNewsBot;
  private final SteamService steamService;
  private final UserService userService;
  private final GameService gameService;
  private static final CopyOnWriteArrayList<NewsItem> newsItems = new CopyOnWriteArrayList<>();
  private static final CopyOnWriteArraySet<Game> problemGames = new CopyOnWriteArraySet<>();
  private static final ConcurrentHashMap<String, String> gamesAppidName = new ConcurrentHashMap<>();

  /**
   * Constructor for SchedulerConfig.
   *
   * @param steamNewsBot the SteamNewsBot instance
   * @param steamService the SteamService instance
   * @param userService  the UserService instance
   * @param gameService  the GameService instance
   */
  public SchedulerConfig(SteamNewsBot steamNewsBot,
                         SteamService steamService,
                         UserService userService,
                         GameService gameService) {
    this.steamNewsBot = steamNewsBot;
    this.steamService = steamService;
    this.userService = userService;
    this.gameService = gameService;
  }

  /**
   * Scheduled task to update and send news items.
   * Runs at a fixed rate of 1800000 milliseconds (30 minutes).
   * Retrieves news items for active games and sends them to users.
   */
  @Scheduled(fixedRate = 1800000)
  private void updateAndSendNewsItems() {
    final Instant startCycle = Instant.now();

    updateNewsItemsList(gameService.getAllGamesByActiveUsers());
    sendNewsItems();

    newsItems.clear();

    if (problemGames.isEmpty()) {
      gamesAppidName.clear();
      logger.info("newsItems list cleared!");
    }

    logger.info("The cycle of updating and sending news is over for {} seconds.",
        Duration.between(startCycle, Instant.now()));
  }

  /**
   * Scheduled task for processing problem games.
   * Runs at a fixed rate of 300000 milliseconds (5 minutes).
   * Retrieves news items for problem games and sends them to users.
   */
  @Scheduled(fixedRate = 300000)
  private void processingProblemGame() {
    if (problemGames.isEmpty()) {
      return;
    }

    logger.info("found {} games", problemGames.size());

    updateNewsItemsList(problemGames);
    sendNewsItems();

    newsItems.clear();
    problemGames.clear();
    gamesAppidName.clear();
    logger.info("newsItems and problem games lists cleared!");
  }

  /**
   * Scheduled task to update the games' database.
   * Runs at a fixed rate of 86400000 milliseconds (24 hours).
   * Updates the user's game data in the database.
   */
  @Scheduled(fixedRate = 86400000)
  private void updateGamesDb() {
    userService.getUsersByActive(true).forEach(user -> {
      try {
        userService.updateUser(
            user.getChatId(), String.valueOf(user.getSteamId()), user.getLocale());
      } catch (IOException e) {
        logger.error("{} error in processing user: {}, with steam ID: {}",
            e.getMessage(), user.getChatId(), user.getSteamId());
      }
    });

    logger.info("GamesDB successful updated! In base {} games.", gameService.countAllGames());
  }

  /**
   * Updates the news items list for the given set of games.
   *
   * @param games the set of games for which to update the news items
   */
  private void updateNewsItemsList(Set<Game> games) {
    if (games.isEmpty()) {
      return;
    }

    logger.info("{} games in list.", games.size());

    final Instant start = Instant.now();

    games.parallelStream()
        .forEach(game -> {
          try {
            gamesAppidName.put(game.getAppid(), game.getName());
            newsItems.addAll(steamService.getNewsByOwnedGames(game.getAppid()));
          } catch (IOException e) {
            problemGames.add(game);
            logger.error("problem with: {}, appid: {}. steam lag", game.getName(), game.getAppid());
          }
        });

    logger.info("getting news is finished for: {} seconds",
        Duration.between(start, Instant.now()).toSeconds());
  }

  /**
   * Sends the news items to users who have subscribed to the corresponding games.
   */
  private void sendNewsItems() {
    if (newsItems.isEmpty()) {
      return;
    }

    logger.info("found {} fresh news", newsItems.size());

    newsItems.stream()
        .filter(newsItem -> !userService.getUsersWithFilters(newsItem.getAppid()).isEmpty())
        .forEach(newsItem -> userService.getUsersWithFilters(newsItem.getAppid())
            .parallelStream()
            .forEach(user -> {
                  logger.info("{} newsItem for user {} is ready!",
                      newsItem.getGid(), user.getName());
                  steamNewsBot.sendNewsMessage(user.getChatId(), "<b>"
                      + gamesAppidName.get(newsItem.getAppid()) + "</b>"
                      + System.lineSeparator() + newsItem, user.getLocale());
                }
            ));
  }
}
