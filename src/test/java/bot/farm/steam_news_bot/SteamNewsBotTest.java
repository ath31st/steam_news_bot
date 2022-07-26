package bot.farm.steam_news_bot;

import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.SendMessageService;
import bot.farm.steam_news_bot.service.UserGameStateService;
import bot.farm.steam_news_bot.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SteamNewsBotTest {

    @Mock
    private SendMessageService sendMessageService;
    @Mock
    private UserService userService;
    @Mock
    private GameService gameService;
    @Mock
    private UserGameStateService userGameStateService;
    @InjectMocks
    private SteamNewsBot steamNewsBot;
    @Mock
    private CallbackQuery callbackQuery;
    @Mock
    private Message message;
    @Mock
    private User user;
    @Mock
    private bot.farm.steam_news_bot.entity.User user1;
    @Mock
    private Update update;
    private String chatId;
    private String text;
    private String locale;


    @BeforeEach
    void setUp() {
        chatId = "160";
        text = "test text";
        locale = "ru";
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getBotUsername() {
        ReflectionTestUtils.setField(steamNewsBot, "BOT_NAME", "bot");
        assertEquals("bot", steamNewsBot.getBotUsername());
    }

    @Test
    void getBotToken() {
        ReflectionTestUtils.setField(steamNewsBot, "BOT_TOKEN", "token");
        assertEquals("token", steamNewsBot.getBotToken());
    }

    @Test
    void onEmptyUpdateReceived() {
        steamNewsBot.onUpdateReceived(update);
        verify(update, times(1)).hasMessage();
        verify(update, times(1)).hasCallbackQuery();
    }

    @Test
    void onUpdateReceived() {
        doReturn(callbackQuery).when(update).getCallbackQuery();
        doReturn(user).when(callbackQuery).getFrom();
        doReturn(message).when(callbackQuery).getMessage();
        doReturn("title\n").when(message).getText();
        doReturn(true).when(update).hasCallbackQuery();
        when(callbackQuery.getMessage().getChatId()).thenReturn(Long.valueOf(chatId));
        when(callbackQuery.getFrom().getLanguageCode()).thenReturn(locale);
        doReturn(true).when(userService).existsByChatId(chatId);
        doReturn(user1).when(userService).getUserByChatId(chatId);
        when(userService.getUserByChatId(chatId).getSteamId()).thenReturn(756L);

        doReturn("/set_active_mode").when(callbackQuery).getData();
        steamNewsBot.onUpdateReceived(update);

        doReturn("/set_inactive_mode").when(callbackQuery).getData();
        steamNewsBot.onUpdateReceived(update);

        doReturn("/check_steam_id").when(callbackQuery).getData();
        steamNewsBot.onUpdateReceived(update);

        doReturn("/unsubscribe").when(callbackQuery).getData();
        steamNewsBot.onUpdateReceived(update);

        doReturn("/links_to_game").when(callbackQuery).getData();
        steamNewsBot.onUpdateReceived(update);

        doReturn("game1").when(gameService).getBanListByChatId(chatId);
        doReturn("/black_list").when(callbackQuery).getData();
        steamNewsBot.onUpdateReceived(update);

        doReturn("").when(gameService).getBanListByChatId(chatId);
        steamNewsBot.onUpdateReceived(update);

        doReturn("/clear_black_list").when(callbackQuery).getData();
        steamNewsBot.onUpdateReceived(update);

        doReturn("game1").when(gameService).getBanListByChatId(chatId);
        steamNewsBot.onUpdateReceived(update);

        doReturn("/set_steam_id").when(callbackQuery).getData();
        steamNewsBot.onUpdateReceived(update);

        verify(callbackQuery, times(20)).getData();
    }

    @Test
    void sendTextMessage() {
        steamNewsBot.sendTextMessage(chatId, text);
        verify(sendMessageService, times(1)).createMessage(chatId, text);
    }

    @Test
    void sendNewsMessage() {
        steamNewsBot.sendNewsMessage(chatId, text, locale);
        verify(sendMessageService, times(1)).createNewsMessage(chatId, text, locale);
    }
}