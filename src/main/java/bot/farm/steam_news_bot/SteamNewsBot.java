package bot.farm.steam_news_bot;


import bot.farm.steam_news_bot.service.*;
import bot.farm.steam_news_bot.util.UserState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

import static bot.farm.steam_news_bot.util.Localization.getMessage;

@Component
@Getter
@Setter
@NoArgsConstructor
public class SteamNewsBot extends TelegramLongPollingBot {
    @Autowired
    private SendMessageService sendMessageService;
    @Autowired
    private ButtonService buttonService;
    @Autowired
    private UserService userService;
    @Autowired
    private SteamService steamService;
    @Autowired
    private GameService gameService;
    @Autowired
    private UserGameStateService userGameStateService;

    private static final Logger logger = LoggerFactory.getLogger(SteamNewsBot.class);
    @Value("${steamnewsbot.botName}")
    private String BOT_NAME;
    @Value("${steamnewsbot.botToken}")
    private String BOT_TOKEN;
    private UserState state = UserState.DEFAULT;

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String chatId;
        String inputText;
        String locale;

        switch (state) {
            case DEFAULT -> {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    chatId = String.valueOf(update.getMessage().getChatId());
                    inputText = update.getMessage().getText();
                    locale = update.getMessage().getFrom().getLanguageCode();

                    switch (inputText) {
                        case "/start" -> sendTextMessage(chatId, getMessage("start", locale));
                        case "/help" -> sendTextMessage(chatId, getMessage("help", locale));
                        case "/settings" -> sendMenuMessage(chatId, getMessage("settings", locale));
                        default -> sendTextMessage(chatId, getMessage("default_message", locale));
                    }
                }
            }
            case SET_STEAM_ID -> {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    chatId = String.valueOf(update.getMessage().getChatId());
                    inputText = update.getMessage().getText().strip();
                    locale = update.getMessage().getFrom().getLanguageCode();

                    if (SteamService.isValidSteamId(inputText)) {

                        sendTextMessage(chatId, getMessage("waiting", locale));

                        try {
                            userService.saveOrUpdateUserInDb(chatId, update.getMessage().getFrom().getUserName(), inputText);

                            sendTextMessage(chatId, String.format(getMessage("registration", locale),
                                    inputText, userService.findUserByChatId(chatId).get().getName(),
                                    userService.getCountOwnedGames(chatId)));

                        } catch (NullPointerException e) {
                            logger.error("User {} entered id {} - this is hidden account", chatId, inputText);
                            sendTextMessage(chatId, String.format(getMessage("error_hidden_acc", locale), inputText));
                        } catch (IOException e) {
                            logger.error("User {} entered id {}, account dont exists", chatId, inputText);
                            sendTextMessage(chatId, String.format(getMessage("error_dont_exists_acc", locale), inputText));
                        }
                    } else {
                        sendTextMessage(chatId, getMessage("incorrect_steam_id", locale));
                    }
                    state = UserState.DEFAULT;
                }
            }

        }

        if (update.hasCallbackQuery()) {
            chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            String callBackData = update.getCallbackQuery().getData();
            locale = update.getCallbackQuery().getFrom().getLanguageCode();

            switch (callBackData) {
                case "/set_steam_id":
                    sendTextMessage(chatId, getMessage("enter_steam_id", locale));
                    state = UserState.SET_STEAM_ID;
                    break;
                case "/check_steam_id":
                    if (userService.existsByChatId(chatId)) {
                        sendTextMessage(chatId, String.format(getMessage("check_steam_id", locale),
                                userService.getUserByChatId(chatId).getSteamId()) +
                                (userService.getUserByChatId(chatId).isActive() ? getMessage("active", locale) : getMessage("inactive", locale)));
                    } else {
                        sendTextMessage(chatId, getMessage("not_registered", locale));
                    }
                    break;
                case "/set_active_mode":
                    if (userService.existsByChatId(chatId)) {
                        userService.updateActiveForUser(chatId, true);
                        sendTextMessage(chatId, getMessage("active_mode",locale));
                    } else {
                        sendTextMessage(chatId, getMessage("not_registered", locale));
                    }
                    break;
                case "/set_inactive_mode":
                    if (userService.existsByChatId(chatId)) {
                        userService.updateActiveForUser(chatId, false);
                        sendTextMessage(chatId, getMessage("inactive_mode", locale));
                    } else {
                        sendTextMessage(chatId, getMessage("not_registered", locale));
                    }
                    break;
                case "/unsubscribe":
                    if (userService.existsByChatId(chatId)) {
                        String gameTitle = update.getCallbackQuery().getMessage().getText();
                        gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
                        if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
                            sendTextMessage(chatId, getMessage("already_unsubscribed", locale) + gameTitle);
                        } else {
                            userGameStateService.updateStateForGameByChatId(chatId, gameTitle, true);
                            sendTextMessage(chatId, getMessage("unsubscribe", locale) + gameTitle);
                        }

                    } else {
                        sendTextMessage(chatId, getMessage("not_registered", locale));
                    }
                    break;
                case "/subscribe":
                    if (userService.existsByChatId(chatId)) {
                        String gameTitle = update.getCallbackQuery().getMessage().getText();
                        gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
                        if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
                            userGameStateService.updateStateForGameByChatId(chatId, gameTitle, false);
                            sendTextMessage(chatId, getMessage("subscribe", locale) + gameTitle);
                        } else {
                            sendTextMessage(chatId, getMessage("already_subscribe", locale) + gameTitle);
                        }
                    } else {
                        sendTextMessage(chatId, getMessage("not_registered", locale));
                    }
                    break;
                case "/black_list":
                    if (userService.existsByChatId(chatId)) {
                        if (gameService.getBanListByChatId(chatId).isBlank()) {
                            sendTextMessage(chatId, getMessage("empty_black_list", locale));
                        } else {
                            sendTextMessage(chatId, getMessage("black_list", locale) + gameService.getBanListByChatId(chatId));
                        }
                    } else {
                        sendTextMessage(chatId, getMessage("not_registered", locale));
                    }
                    break;
                case "/clear_black_list":
                    if (userService.existsByChatId(chatId)) {
                        if (gameService.getBanListByChatId(chatId).isBlank()) {
                            sendTextMessage(chatId, getMessage("empty_black_list", locale));
                        } else {
                            userGameStateService.getBlackListByChatId(chatId)
                                    .forEach(state -> userGameStateService.updateStateForGameById(false, state.getId()));
                            sendTextMessage(chatId, getMessage("black_list_clear", locale));
                        }
                    } else {
                        sendTextMessage(chatId, getMessage("not_registered", locale));
                    }
                    break;
                default:
                    sendTextMessage(chatId, getMessage("default_message", locale));
                    break;
            }
        }
    }

    public void sendTextMessage(String chatId, String text) {
        try {
            execute(sendMessageService.createMessage(chatId, text));
        } catch (TelegramApiException e) {
            if (e.getMessage().endsWith("[403] Forbidden: bot was blocked by the user")) {
                userService.updateActiveForUser(chatId, false);

                logger.info(String.format("User with chatId %s has received the \"inactive\" status", chatId));
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendNewsMessage(String chatId, String text) {
        try {
            execute(sendMessageService.createNewsMessage(chatId, text));
        } catch (TelegramApiException e) {
            if (e.getMessage().endsWith("[403] Forbidden: bot was blocked by the user")) {
                userService.updateActiveForUser(chatId, false);

                logger.info(String.format("User with chatId %s has received the \"inactive\" status", chatId));
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendMenuMessage(String chatId, String message) {
        try {
            execute(sendMessageService.createMenuMessage(chatId, message));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
