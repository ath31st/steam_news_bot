package bot.farm.steam_news_bot;


import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.service.ButtonService;
import bot.farm.steam_news_bot.service.SendMessageService;
import bot.farm.steam_news_bot.service.SteamService;
import bot.farm.steam_news_bot.service.UserService;
import bot.farm.steam_news_bot.util.UserState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
                        User user = new User();
                        user.setChatId(chatId);
                        user.setName(update.getMessage().getFrom().getUserName());
                        user.setSteamId(Long.valueOf(inputText));
                        userService.saveOrUpdateUserInDb(user);
                        sendTextMessage(chatId, "Your steam ID: " + inputText);
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
                        sendTextMessage(chatId, "Your steam ID: " + userService.findUserByChatId(chatId).get().getSteamId());
    //                    steamService.getOwnedGames(userService.findUserByChatId(chatId).get().getSteamId()).forEach(System.out::println);
                    } else {
                        sendTextMessage(chatId, "You are not registered yet. Please select Set/Update steam ID");
                    }
                    break;
                default:
                    sendTextMessage(chatId, "Wrong query");
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
