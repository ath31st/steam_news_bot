package bot.farm.steam_news_bot;


import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.service.ButtonService;
import bot.farm.steam_news_bot.service.SendMessageService;
import bot.farm.steam_news_bot.service.UserService;
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

    @Value("${steamnewsbot.botName}")
    private String BOT_NAME;
    @Value("${steamnewsbot.botToken}")
    private String BOT_TOKEN;

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
            String chatId = String.valueOf(update.getMessage().getChatId());
            String inputText = update.getMessage().getText();

            switch (inputText) {
                case "/start":
                    sendTextMessage(chatId, START);
                    break;
                case "/help":
                    sendTextMessage(chatId, HELP);
                    break;
                case "/settings":
                    try {
                        execute(sendMessageService.createMenuMessage(chatId));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                default:
                    sendTextMessage(chatId, WRONG_COMMAND);
                    break;
            }
        }

        if (update.hasCallbackQuery()) {
            String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            String callBackData = update.getCallbackQuery().getData();

            switch (callBackData) {
                case "/set_steam_id":
                    sendTextMessage(chatId, "Enter your Steam ID");
                    User user = new User();
                    user.setChatId(chatId);
                    user.setSteamId(1234L);
                    userService.saveUserInDb(user);
                    break;
                default:
                    sendTextMessage(chatId, "Wrong query");
                    break;

            }
            //  executeCommands(chatId);
        }
    }

    private void sendTextMessage(String chatId, String text) {
        try {
            execute(sendMessageService.createMessage(chatId, text));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
//    private void executeCommands(String chatId) {
//        try {
//
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
