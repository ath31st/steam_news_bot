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

import static bot.farm.steam_news_bot.util.Localization.Messages.*;
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
                        case "/start" -> sendTextMessage(chatId, getMessage(START, locale));
                        case "/help" -> sendTextMessage(chatId, getMessage(HELP, locale));
                        case "/settings" -> sendMenuMessage(chatId, getMessage(SETTINGS, locale));
                        default -> sendTextMessage(chatId, getMessage(DEFAULT_MESSAGE, locale));
                    }
                }
            }
            case SET_STEAM_ID -> {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    chatId = String.valueOf(update.getMessage().getChatId());
                    inputText = update.getMessage().getText().strip();
                    locale = update.getMessage().getFrom().getLanguageCode();

                    if (SteamService.isValidSteamId(inputText)) {

                        sendTextMessage(chatId, getMessage(WAITING, locale));

                        try {
                            userService.saveOrUpdateUserInDb(chatId, update.getMessage().getFrom().getUserName(), inputText, locale);
                            if (userService.findUserByChatId(chatId).isPresent()) {
                                sendTextMessage(chatId, String.format(getMessage(REGISTRATION, locale),
                                        inputText, userService.findUserByChatId(chatId).get().getName(),
                                        userService.getCountOwnedGames(chatId)));
                            }
                        } catch (NullPointerException e) {
                            logger.error("User {} entered id {} - this is hidden account", chatId, inputText);
                            sendTextMessage(chatId, String.format(getMessage(ERROR_HIDDEN_ACC, locale), inputText));
                        } catch (IOException e) {
                            logger.error("User {} entered id {}, account dont exists", chatId, inputText);
                            sendTextMessage(chatId, String.format(getMessage(ERROR_DONT_EXISTS_ACC, locale), inputText));
                        }
                    } else {
                        sendTextMessage(chatId, getMessage(INCORRECT_STEAM_ID, locale));
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
                    sendTextMessage(chatId, getMessage(ENTER_STEAM_ID, locale));
                    state = UserState.SET_STEAM_ID;
                    break;
                case "/check_steam_id":
                    if (userService.existsByChatId(chatId)) {
                        sendTextMessage(chatId, String.format(getMessage(CHECK_STEAM_ID, locale),
                                userService.getUserByChatId(chatId).getSteamId()) +
                                (userService.getUserByChatId(chatId).isActive() ? getMessage(ACTIVE, locale) : getMessage(INACTIVE, locale)));
                    } else {
                        sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
                    }
                    break;
                case "/set_active_mode":
                    if (userService.existsByChatId(chatId)) {
                        userService.updateActiveForUser(chatId, true);
                        sendTextMessage(chatId, getMessage(ACTIVE_MODE, locale));
                    } else {
                        sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
                    }
                    break;
                case "/set_inactive_mode":
                    if (userService.existsByChatId(chatId)) {
                        userService.updateActiveForUser(chatId, false);
                        sendTextMessage(chatId, getMessage(INACTIVE_MODE, locale));
                    } else {
                        sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
                    }
                    break;
                case "/unsubscribe":
                    if (userService.existsByChatId(chatId)) {
                        String gameTitle = update.getCallbackQuery().getMessage().getText();
                        gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
                        if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
                            sendTextMessage(chatId, getMessage(ALREADY_UNSUBSCRIBED, locale) + gameTitle);
                        } else {
                            userGameStateService.updateStateForGameByChatId(chatId, gameTitle, true);
                            sendTextMessage(chatId, getMessage(UNSUBSCRIBED, locale) + gameTitle);
                        }

                    } else {
                        sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
                    }
                    break;
                case "/subscribe":
                    if (userService.existsByChatId(chatId)) {
                        String gameTitle = update.getCallbackQuery().getMessage().getText();
                        gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
                        if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
                            userGameStateService.updateStateForGameByChatId(chatId, gameTitle, false);
                            sendTextMessage(chatId, getMessage(SUBSCRIBED, locale) + gameTitle);
                        } else {
                            sendTextMessage(chatId, getMessage(ALREADY_SUBSCRIBED, locale) + gameTitle);
                        }
                    } else {
                        sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
                    }
                    break;
                case "/black_list":
                    if (userService.existsByChatId(chatId)) {
                        if (gameService.getBanListByChatId(chatId).isBlank()) {
                            sendTextMessage(chatId, getMessage(EMPTY_BLACK_LIST, locale));
                        } else {
                            sendTextMessage(chatId, getMessage(BLACK_LIST, locale) + gameService.getBanListByChatId(chatId));
                        }
                    } else {
                        sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
                    }
                    break;
                case "/clear_black_list":
                    if (userService.existsByChatId(chatId)) {
                        if (gameService.getBanListByChatId(chatId).isBlank()) {
                            sendTextMessage(chatId, getMessage(EMPTY_BLACK_LIST, locale));
                        } else {
                            userGameStateService.getBlackListByChatId(chatId)
                                    .forEach(state -> userGameStateService.updateStateForGameById(false, state.getId()));
                            sendTextMessage(chatId, getMessage(BLACK_LIST_CLEAR, locale));
                        }
                    } else {
                        sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
                    }
                    break;
                default:
                    sendTextMessage(chatId, getMessage(DEFAULT_MESSAGE, locale));
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
