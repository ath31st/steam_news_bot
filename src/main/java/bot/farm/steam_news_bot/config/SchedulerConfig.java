package bot.farm.steam_news_bot.config;

import bot.farm.steam_news_bot.SteamNewsBot;
import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.NewsItem;
import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.SteamService;
import bot.farm.steam_news_bot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private static final ConcurrentHashMap<String, String> gamesAppidName = new ConcurrentHashMap<>();

    public SchedulerConfig(SteamNewsBot steamNewsBot,
                           SteamService steamService,
                           UserService userService,
                           GameService gameService) {
        this.steamNewsBot = steamNewsBot;
        this.steamService = steamService;
        this.userService = userService;
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 1800000)
    private void updateAndSendNewsItems() {
        Instant startCycle = Instant.now();

        updateNewsItemsList(gameService.getAllGamesByActiveUsers());
        sendNewsItems();

        logger.info("The cycle of updating and sending news is over for " + Duration.between(startCycle, Instant.now()) + " seconds.");
    }

    @Scheduled(fixedRate = 86400000)
    private void updateGamesDb() {
        List<User> users = userService.getUsersByActive(true);
        users.forEach(user -> {
            try {
                userService.updateUser(user.getChatId(), String.valueOf(user.getSteamId()), user.getLocale());
            } catch (IOException e) {
                logger.error(e.getMessage() + " error in processing user: {}, with steam ID: {}", user.getChatId(), user.getSteamId());
                throw new RuntimeException(e);
            }
        });

        logger.info(String.format("GamesDB successful updated! In base %d games.", gameService.countAllGames()));
    }

    private void updateNewsItemsList(Set<Game> games) {
        if (games.isEmpty())
            return;

        logger.info(String.format("%d games in list.", games.size()));

        Instant start = Instant.now();
        games.parallelStream()
                .forEach(game -> {
                    newsItems.addAll(steamService.getNewsByOwnedGames(game.getAppid()));
                    gamesAppidName.put(game.getAppid(), game.getName());
                });

        logger.info("getting news is finished for: " + Duration.between(start, Instant.now()).toSeconds() + " seconds");

    }

    private void sendNewsItems() {
        if (newsItems.isEmpty())
            return;

        logger.info(String.format("found %d fresh news", newsItems.size()));

        for (NewsItem newsItem : newsItems) {
            if (!userService.getUsersWithFilters(newsItem.getAppid()).isEmpty()) {
                userService.getUsersWithFilters(newsItem.getAppid())
                        .stream()
                        .parallel()
                        .peek(user -> logger.info(newsItem.getGid() + " newsItem for user " + user.getName() + " is ready!"))
                        .forEach(user ->
                                steamNewsBot.sendNewsMessage(user.getChatId(), "<b>"
                                        + gamesAppidName.get(newsItem.getAppid()) + "</b>" + System.lineSeparator() + newsItem, user.getLocale()));
            }
        }
        newsItems.clear();
        gamesAppidName.clear();

        logger.info("newsItems list cleared!");
    }

}
