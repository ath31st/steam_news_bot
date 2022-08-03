package bot.farm.steam_news_bot.config;

import bot.farm.steam_news_bot.SteamNewsBot;
import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.NewsItem;
import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.SendMessageService;
import bot.farm.steam_news_bot.service.SteamService;
import bot.farm.steam_news_bot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
    private final SteamNewsBot steamNewsBot;
    private final SteamService steamService;
    private final UserService userService;
    private final SendMessageService sendMessageService;
    private final GameService gameService;
    private static final List<NewsItem> newsItems = new ArrayList<>();
    private static final HashMap<String, String> gamesAppidName = new HashMap<>();

    public SchedulerConfig(SteamNewsBot steamNewsBot,
                           SteamService steamService,
                           UserService userService,
                           SendMessageService sendMessageService,
                           GameService gameService) {
        this.steamNewsBot = steamNewsBot;
        this.steamService = steamService;
        this.userService = userService;
        this.sendMessageService = sendMessageService;
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 1800000)
    private void updateAndSendNewsItems() {
        Instant startCycle = Instant.now();

        Set<Game> games = gameService.getAllGamesByActiveUsers();

        if (!games.isEmpty()) {

            logger.info(String.format("%d games in list.", games.size()));
            Instant start = Instant.now();
            for (Game game : games) {
                newsItems.addAll(steamService.getNewsByOwnedGames(game.getAppid()));
                gamesAppidName.put(game.getAppid(), game.getName());
            }

            logger.info("getting news is finished for: " + Duration.between(start, Instant.now()).toSeconds() + " seconds");
        }
        if (!newsItems.isEmpty()) {

            logger.info(String.format("found %d fresh news", newsItems.size()));

            for (NewsItem newsItem : newsItems) {
                if (!userService.getUsersByAppid(newsItem.getAppid()).isEmpty()) {
                    userService.getUsersByAppid(newsItem.getAppid())
                            .stream()
                            .peek(user -> logger.info(newsItem.getGid() + " newsItem for user " + user.getName() + " is ready!"))
                            .forEach(user -> steamNewsBot.sendTextMessage(user.getChatId(),
                                    "<b>" + gamesAppidName.get(newsItem.getAppid()) + "</b>" + System.lineSeparator() + newsItem));
                }
            }
            newsItems.clear();
            gamesAppidName.clear();
            logger.info("newsItems list cleared!");
        }
        logger.info("The cycle of updating and sending news is over for " + Duration.between(startCycle, Instant.now()) + " seconds.");
    }

    @Scheduled(fixedRate = 86400000)
    private void updateGamesDb() {
        List<User> users = userService.getUsersByActive(true);
        users.forEach(user -> gameService.saveGamesInDb(user.getGames()));

        logger.info(String.format("GamesDB successful updated! In base %d games.", gameService.getAllGames().size()));
    }

}
