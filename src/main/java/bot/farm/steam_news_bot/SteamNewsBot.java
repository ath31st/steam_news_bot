package bot.farm.steam_news_bot;


import bot.farm.steam_news_bot.service.*;
import bot.farm.steam_news_bot.util.UserState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

import static bot.farm.steam_news_bot.localization.message.MessageEnum.*;
import static bot.farm.steam_news_bot.localization.message.MessageLocalization.getMessage;

@Component
public class SteamNewsBot extends TelegramLongPollingBot {
    private final SendMessageService sendMessageService;
    private final UserService userService;
    private final GameService gameService;
    private final UserGameStateService userGameStateService;

    private static final Logger logger = LoggerFactory.getLogger(SteamNewsBot.class);
    @Value("${steamnewsbot.botName}")
    private String BOT_NAME;
    @Value("${steamnewsbot.botToken}")
    private String BOT_TOKEN;
    private UserState state = UserState.DEFAULT;

    public SteamNewsBot(SendMessageService sendMessageService,
                        UserService userService,
                        GameService gameService,
                        UserGameStateService userGameStateService) {
        this.sendMessageService = sendMessageService;
        this.userService = userService;
        this.gameService = gameService;
        this.userGameStateService = userGameStateService;
    }

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

        if (update.hasMessage() && update.getMessage().hasText()) {
            messageProcessing(update.getMessage());
        }

        if (update.hasCallbackQuery()) {
            callBackQueryProcessing(update.getCallbackQuery());
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
                logger.error(e.getMessage());
            }
        }
    }

    public void sendNewsMessage(String chatId, String text, String locale) {
        try {
            execute(sendMessageService.createNewsMessage(chatId, text, locale));
        } catch (TelegramApiException e) {
            if (e.getMessage().endsWith("[403] Forbidden: bot was blocked by the user")) {
                userService.updateActiveForUser(chatId, false);

                logger.info(String.format("User with chatId %s has received the \"inactive\" status", chatId));
            } else {
                logger.error(e.getMessage());
            }
        }
    }

    private void sendMenuMessage(String chatId, String message, String locale) {
        try {
            execute(sendMessageService.createMenuMessage(chatId, message, locale));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void setUpdateSteamId(String chatId, String inputText, String locale, String username) {
        if (!SteamService.isValidSteamId(inputText)) {
            sendTextMessage(chatId, getMessage(INCORRECT_STEAM_ID, locale));
            return;
        }

        sendTextMessage(chatId, getMessage(WAITING, locale));

        try {
            if (userService.existsByChatId(chatId)) {
                userService.updateUser(chatId, inputText, locale);
            } else {
                userService.saveUser(chatId, username, inputText, locale);
            }

            sendMessageAfterSetUpdateSteamId(chatId, inputText, locale);
        } catch (NullPointerException e) {
            logger.error("User {} entered id {} - this is hidden account", chatId, inputText);
            sendTextMessage(chatId, String.format(getMessage(ERROR_HIDDEN_ACC, locale), inputText));
        } catch (IOException e) {
            logger.error("User {} entered id {}, account dont exists", chatId, inputText);
            sendTextMessage(chatId, String.format(getMessage(ERROR_DONT_EXISTS_ACC, locale), inputText));
        }
    }

    private void sendMessageAfterSetUpdateSteamId(String chatId, String inputText, String locale) {
        if (userService.findUserByChatId(chatId).isEmpty()) return;
        sendTextMessage(chatId, String.format(getMessage(REGISTRATION, locale),
                inputText, userService.findUserByChatId(chatId).get().getName(),
                userService.getCountOwnedGames(chatId)));
    }

    private void messageProcessing(Message message) {
        String chatId = String.valueOf(message.getChatId());
        String inputText = message.getText();
        String locale = message.getFrom().getLanguageCode();

        switch (state) {
            case DEFAULT -> {
                switch (inputText) {
                    case "/start" -> sendTextMessage(chatId, getMessage(START, locale));
                    case "/help" -> sendTextMessage(chatId, getMessage(HELP, locale));
                    case "/settings" -> sendMenuMessage(chatId, getMessage(SETTINGS, locale), locale);
                    default -> sendTextMessage(chatId, getMessage(DEFAULT_MESSAGE, locale));
                }
            }
            case SET_STEAM_ID -> {
                setUpdateSteamId(chatId, inputText, locale, message.getFrom().getUserName());

                state = UserState.DEFAULT;
            }
        }
    }

    private void callBackQueryProcessing(CallbackQuery callbackQuery) {
        String chatId = String.valueOf(callbackQuery.getMessage().getChatId());
        String locale = callbackQuery.getFrom().getLanguageCode();

        if (!userService.existsByChatId(chatId) & !callbackQuery.getData().equals("/set_steam_id")) {
            sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
            return;
        }

        switch (callbackQuery.getData()) {
            case "/set_steam_id" -> {
                sendTextMessage(chatId, getMessage(ENTER_STEAM_ID, locale));
                state = UserState.SET_STEAM_ID;
            }
            case "/check_steam_id" -> sendTextMessage(chatId, String.format(getMessage(CHECK_STEAM_ID, locale),
                    userService.getUserByChatId(chatId).getSteamId()) +
                    (userService.getUserByChatId(chatId).isActive() ? getMessage(ACTIVE, locale) : getMessage(INACTIVE, locale)));

            case "/set_active_mode" -> {
                userService.updateActiveForUser(chatId, true);
                sendTextMessage(chatId, getMessage(ACTIVE_MODE, locale));
            }
            case "/set_inactive_mode" -> {
                userService.updateActiveForUser(chatId, false);
                sendTextMessage(chatId, getMessage(INACTIVE_MODE, locale));
            }
            case "/unsubscribe" -> {
                String gameTitle = callbackQuery.getMessage().getText();
                gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
                if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
                    sendTextMessage(chatId, getMessage(ALREADY_UNSUBSCRIBED, locale) + gameTitle);
                } else {
                    userGameStateService.updateStateForGameByChatId(chatId, gameTitle, true);
                    sendTextMessage(chatId, getMessage(UNSUBSCRIBE, locale) + gameTitle);
                }
            }
            case "/subscribe" -> {
                String gameTitle = callbackQuery.getMessage().getText();
                gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
                if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
                    userGameStateService.updateStateForGameByChatId(chatId, gameTitle, false);
                    sendTextMessage(chatId, getMessage(SUBSCRIBE, locale) + gameTitle);
                } else {
                    sendTextMessage(chatId, getMessage(ALREADY_SUBSCRIBED, locale) + gameTitle);
                }
            }
            case "/black_list" -> {
                if (gameService.getBanListByChatId(chatId).isBlank()) {
                    sendTextMessage(chatId, getMessage(EMPTY_BLACK_LIST, locale));
                } else {
                    sendTextMessage(chatId, getMessage(BLACK_LIST, locale) + gameService.getBanListByChatId(chatId));
                }
            }
            case "/clear_black_list" -> {
                if (gameService.getBanListByChatId(chatId).isBlank()) {
                    sendTextMessage(chatId, getMessage(EMPTY_BLACK_LIST, locale));
                } else {
                    userGameStateService.getBlackListByChatId(chatId)
                            .forEach(state -> userGameStateService.updateStateForGameById(false, state.getId()));
                    sendTextMessage(chatId, getMessage(BLACK_LIST_CLEAR, locale));
                }
            }
            default -> sendTextMessage(chatId, getMessage(DEFAULT_MESSAGE, locale));

        }
    }

}
