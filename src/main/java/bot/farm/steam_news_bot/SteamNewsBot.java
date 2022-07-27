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

import static bot.farm.steam_news_bot.util.Constants.*;

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

        switch (state) {
            case DEFAULT -> {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    chatId = String.valueOf(update.getMessage().getChatId());
                    inputText = update.getMessage().getText();

                    switch (inputText) {
                        case "/start" -> sendTextMessage(chatId, START);
                        case "/help" -> sendTextMessage(chatId, HELP);
                        case "/settings" -> sendMenuMessage(chatId);
                        default -> sendTextMessage(chatId, WRONG_COMMAND);
                    }
                }
            }
            case SET_STEAM_ID -> {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    chatId = String.valueOf(update.getMessage().getChatId());
                    inputText = update.getMessage().getText().strip();
                    if (SteamService.isValidSteamId(inputText)) {

                        gameService.saveGamesInDb(steamService.getOwnedGames(Long.valueOf(inputText)));
                        userService.saveOrUpdateUserInDb(chatId, update.getMessage().getFrom().getUserName(), inputText);

                        StringBuilder sb = new StringBuilder();
                        sb.append("Your steam ID: ")
                                .append(inputText)
                                .append(System.lineSeparator())
                                .append("Hi ").append(userService.findUserByChatId(chatId).get().getName()).append("!")
                                .append(System.lineSeparator())
                                .append("Nice library! You have ")
                                .append(userService.findUserByChatId(chatId).get().getGames().size())
                                .append(" owned games on your account");
                        sendTextMessage(chatId, sb.toString());
                    } else {
                        sendTextMessage(chatId, "You entered an incorrect steam ID");
                    }
                    state = UserState.DEFAULT;
                }
            }

        }

        if (update.hasCallbackQuery()) {
            chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            String callBackData = update.getCallbackQuery().getData();

            switch (callBackData) {
                case "/set_steam_id":
                    sendTextMessage(chatId, "Enter your Steam ID");
                    state = UserState.SET_STEAM_ID;
                    break;
                case "/check_steam_id":
                    if (userService.findUserByChatId(chatId).isPresent()) {
                        sendTextMessage(chatId, "Your steam ID: " + userService.findUserByChatId(chatId).get().getSteamId() +
                                "\n" + "Status: " + (userService.findUserByChatId(chatId).get().isActive() ? "active" : "inactive"));
                    } else {
                        sendTextMessage(chatId, "You are not registered yet. Please select Set/Update steam ID");
                    }
                    break;
                case "/set_active_mode":
                    if (userService.findUserByChatId(chatId).isPresent()) {
                        userService.updateActiveForUser(chatId, true);
                        sendTextMessage(chatId, "You are set \"active\" mode. Now the bot will send you news");
                    } else {
                        sendTextMessage(chatId, "You are not registered yet. Please select Set/Update steam ID");
                    }
                    break;
                case "/set_inactive_mode":
                    if (userService.findUserByChatId(chatId).isPresent()) {
                        userService.updateActiveForUser(chatId, false);
                        sendTextMessage(chatId, "You are set \"inactive\" mode. " +
                                "Now the bot will not send you news until you activate the \"active\" mode again");
                    } else {
                        sendTextMessage(chatId, "You are not registered yet. Please select Set/Update steam ID");
                    }
                    break;
                default:
                    sendTextMessage(chatId, WRONG_COMMAND);
                    break;
            }
        }
    }

    private void sendTextMessage(String chatId, String text) {
        try {
            execute(sendMessageService.createMessage(chatId, text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMenuMessage(String chatId) {
        try {
            execute(sendMessageService.createMenuMessage(chatId));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


}
