package bot.farm.steam_news_bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.SendMessageService;
import bot.farm.steam_news_bot.service.UserGameStateService;
import bot.farm.steam_news_bot.service.UserService;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

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
    ReflectionTestUtils.setField(steamNewsBot, "botName", "bot");
    assertEquals("bot", steamNewsBot.getBotUsername());
  }

  @Test
  void getBotToken() {
    ReflectionTestUtils.setField(steamNewsBot, "botToken", "token");
    assertEquals("token", steamNewsBot.getBotToken());
  }

  @Test
  void onEmptyUpdateReceived() {
    steamNewsBot.onUpdateReceived(update);
    verify(update, times(1)).hasMessage();
    verify(update, times(1)).hasCallbackQuery();
  }

  static Stream<String> messageTestData() {
    return Stream.of(
        "start",
        "/start",
        "/settings",
        "/help",
        "/no_command"
    );
  }

  @ParameterizedTest
  @MethodSource("messageTestData")
  void onUpdateReceived_message(String messageText) {
    Message message = mock(Message.class);
    User userTlg = mock(User.class);

    when(update.hasMessage()).thenReturn(true);
    when(update.getMessage()).thenReturn(message);
    when(message.hasText()).thenReturn(true);
    when(message.getFrom()).thenReturn(userTlg);
    when(message.getText()).thenReturn(messageText);
    when(message.getFrom().getLanguageCode()).thenReturn("ru");

    steamNewsBot.onUpdateReceived(update);

    verify(update, times(1)).hasMessage();
  }

//  @Test
//  void onUpdateReceived() {
//    doReturn(callbackQuery).when(update).getCallbackQuery();
//    doReturn(user).when(callbackQuery).getFrom();
//    doReturn(message).when(callbackQuery).getMessage();
//    doReturn("title\n").when(message).getText();
//    doReturn(true).when(update).hasCallbackQuery();
//    when(callbackQuery.getMessage().getChatId()).thenReturn(Long.valueOf(chatId));
//    when(callbackQuery.getFrom().getLanguageCode()).thenReturn(locale);
//    doReturn(true).when(userService).existsByChatId(chatId);
//    doReturn(user1).when(userService).getUserByChatId(chatId);
//    when(userService.getUserByChatId(chatId).getSteamId()).thenReturn(756L);
//
//    doReturn("/set_active_mode").when(callbackQuery).getData();
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("/set_inactive_mode").when(callbackQuery).getData();
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("/check_steam_id").when(callbackQuery).getData();
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("/unsubscribe").when(callbackQuery).getData();
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("/links_to_game").when(callbackQuery).getData();
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("game1").when(gameService).getBanListByChatId(chatId);
//    doReturn("/black_list").when(callbackQuery).getData();
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("").when(gameService).getBanListByChatId(chatId);
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("/clear_black_list").when(callbackQuery).getData();
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("game1").when(gameService).getBanListByChatId(chatId);
//    steamNewsBot.onUpdateReceived(update);
//
//    doReturn("/set_steam_id").when(callbackQuery).getData();
//    steamNewsBot.onUpdateReceived(update);
//
//    verify(callbackQuery, times(20)).getData();
//  }

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