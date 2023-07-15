package bot.farm.steam_news_bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.repository.UserRepository;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private UserGameStateService userGameStateService;
  @Mock
  private SteamService steamService;
  @InjectMocks
  private UserService userService;

  private String chatId;
  private String name;
  private String streamId;
  private String locale;
  private boolean active;

  @BeforeEach
  void setUp() {
    chatId = "1";
    name = "Test";
    streamId = "76561198131767661";
    locale = "ru";
    active = true;
  }

  @Test
  void saveUser() throws IOException {
    doReturn(Optional.empty()).when(userRepository).findUserByChatId(chatId);
    userService.saveUser(chatId, name, streamId, locale);
    verify(userRepository, times(1)).findUserByChatId(chatId);
  }

  @Test
  void updateUser() throws IOException {
    userService.updateUser(chatId, streamId, locale);
    verify(userRepository, times(1)).findUserByChatId(chatId);
  }

  @Test
  void getCountOwnedGames() {
    doReturn(100L).when(userRepository).countByChatIdAndStates_IsOwnedTrue(chatId);
    assertEquals(100L, userService.getCountOwnedGames(chatId));
  }

  @Test
  void updateActiveForUser() {
    doReturn(Optional.of(new User())).when(userRepository).findUserByChatId(chatId);
    userService.updateActiveForUser(chatId, active);
    verify(userRepository, times(1)).findUserByChatId(chatId);
    verify(userRepository, times(1)).updateActiveByChatId(active, chatId);
  }

  @Test
  void findUserByChatId() {
    userService.findUserByChatId(chatId);
    verify(userRepository, times(1)).findUserByChatId(chatId);
  }

  @Test
  void getAllUsers() {
    assertEquals(0, userService.getAllUsers().size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  void getUsersWithFilters() {
    String appid = "440";
    assertEquals(0, userService.getUsersWithFilters(appid).size());
    verify(userRepository, times(1)).findByActiveTrueAndStates_Game_AppidAndStates_IsBannedFalse(appid);
  }

  @Test
  void getUsersByActive() {
    assertEquals(0, userService.getUsersByActive(active).size());
    verify(userRepository, times(1)).findByActive(active);
  }

  @Test
  void countUsersByActive() {
    doReturn(10L).when(userRepository).countByActive(active);
    assertEquals(10L, userService.countUsersByActive(active));
  }

  @Test
  void existsByChatId() {
    assertFalse(userService.existsByChatId(chatId));
  }

  @Test
  void getUserByChatId() {
    User user = new User();
    user.setLocale(locale);
    user.setChatId(chatId);
    user.setActive(active);
    user.setName(name);
    user.setSteamId(Long.valueOf(streamId));

    doReturn(user).when(userRepository).findByChatId(chatId);
    userService.getUserByChatId(chatId);
    verify(userRepository, times(1)).findByChatId(chatId);
  }

  @Test
  void checkBanForGameByChatId() {
    assertFalse(userService.checkBanForGameByChatId(chatId, name));
    verify(userRepository, times(1)).existsByChatIdAndStates_Game_NameAndStates_IsBannedTrue(chatId, name);
  }

  @Test
  void updateSetStates() throws IOException {
    User user = new User();
    user.setSteamId(Long.valueOf(streamId));
    Optional<User> optionalUser = Optional.of(user);

    doReturn(optionalUser).when(userRepository).findUserByChatId(chatId);

    userService.updateUser(chatId, streamId, locale);
    verify(userRepository, times(2)).findUserByChatId(chatId);
  }

}