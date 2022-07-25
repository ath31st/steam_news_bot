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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

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

    @Scheduled(fixedDelay = 1800000)
    private void updateAndSendNewsItems() {
        List<Game> games = gameService.getAllGames();

        if (!games.isEmpty()) {

            logger.info(String.format("%d games in list.", games.size()));

            for (Game game : games) {
                newsItems.addAll(steamService.getNewsByOwnedGames(game.getAppid()));
            }
        }
        if (!newsItems.isEmpty()) {

            logger.info(String.format("found %d fresh news", newsItems.size()));

            for (NewsItem newsItem : newsItems) {

                logger.info(String.format("%d news in list.", newsItems.size()));

                if (!userService.getUsersByAppid(newsItem.getAppid()).isEmpty()) {
                    userService.getUsersByAppid(newsItem.getAppid())
                            .forEach(user -> sendTextMessage(user.getChatId(), newsItem.toString()));
                }
            }
            newsItems.clear();
            logger.info("newsItems list cleared!");
        }
        logger.info("The cycle of updating and sending news is over");
    }

    @Scheduled(fixedDelay = 86400000)
    private void updateGamesDb() {
        List<User> users = userService.getAllUsers();
        users.forEach(user -> gameService.saveGamesInDb(user.getGamesAppids()));

        logger.info(String.format("GamesDB successful updated! In base %d games.", gameService.getAllGames().size()));
    }

    private void sendTextMessage(String chatId, String text) {
        try {
            steamNewsBot.execute(sendMessageService.createMessage(chatId, text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
