package bot.farm.steam_news_bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import bot.farm.steam_news_bot.repository.UserRepository;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
  private String steamId;
  private String locale;
  private boolean active;

  @BeforeEach
  void setUp() {
    chatId = "1";
    name = "Test";
    steamId = "76561198131767661";
    locale = "ru";
    active = true;
  }

  @Test
  void saveUser() throws IOException {
    userService.saveUser(chatId, name, steamId, locale);
    verify(userRepository, times(1)).save(any());
  }

  @Test
  void saveUser_withNullName() throws IOException {
    userService.saveUser(chatId, null, steamId, locale);
    verify(userRepository, times(1)).save(any());
  }

  @Test
  void updateUser() throws IOException {
    User user = new User();
    user.setChatId(chatId);

    when(userRepository.findByChatId(chatId)).thenReturn(user);

    userService.updateUser(chatId, steamId, locale);
    verify(userRepository, times(1)).save(any());
  }

  @Test
  void getCountOwnedGames() {
    doReturn(100L).when(userRepository).countByChatIdAndStates_IsOwnedTrue(chatId);
    assertEquals(100L, userService.getCountOwnedGames(chatId));
  }

  @Test
  void updateActiveForUser() {
    when(userRepository.existsByChatId(chatId)).thenReturn(true);

    userService.updateActiveForUser(chatId, active);
    verify(userRepository, times(1)).updateActiveByChatId(active, chatId);
  }

  @Test
  void updateActiveForUser_whenUserNotExists() {
    when(userRepository.existsByChatId(chatId)).thenReturn(false);

    userService.updateActiveForUser(chatId, active);
    verify(userRepository, times(0)).updateActiveByChatId(active, chatId);
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
    user.setSteamId(Long.valueOf(steamId));

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
  void updateSetState_whenGameIsOwnedAndWished() throws IOException {
    Game game = new Game();
    UserGameState newUgs = new UserGameState();
    newUgs.setGame(game);
    newUgs.setOwned(true);
    newUgs.setWished(true);
    UserGameState oldUgs = new UserGameState();
    oldUgs.setGame(game);
    oldUgs.setOwned(false);
    oldUgs.setWished(true);

    User user = new User();
    user.setChatId(chatId);
    user.setStates(new HashSet<>(Set.of(oldUgs)));

    when(userGameStateService.getSetStatesByUser(user)).thenReturn(new HashSet<>(Set.of(newUgs)));
    when(userGameStateService.existsByUserAndGame(newUgs.getUser(), newUgs.getGame())).thenReturn(true);
    when(userGameStateService.findByUserAndGame(newUgs.getUser(), newUgs.getGame())).thenReturn(newUgs);
    when(userRepository.findByChatId(chatId)).thenReturn(user);

    userService.updateUser(chatId, steamId, locale);
    verify(userRepository, times(1)).save(any());
    assertTrue(newUgs.isOwned());
    assertFalse(newUgs.isWished());
  }

  @Test
  void updateSetState_whenGameIsNotOwnedButWished() throws IOException {
    Game game = new Game();
    UserGameState newUgs = new UserGameState();
    newUgs.setGame(game);
    newUgs.setOwned(false);
    newUgs.setWished(true);
    UserGameState oldUgs = new UserGameState();
    oldUgs.setGame(game);
    oldUgs.setOwned(false);
    oldUgs.setWished(false);

    User user = new User();
    user.setChatId(chatId);
    user.setStates(new HashSet<>(Set.of(oldUgs)));

    when(userGameStateService.getSetStatesByUser(user)).thenReturn(new HashSet<>(Set.of(newUgs)));
    when(userGameStateService.existsByUserAndGame(newUgs.getUser(), newUgs.getGame())).thenReturn(true);
    when(userGameStateService.findByUserAndGame(newUgs.getUser(), newUgs.getGame())).thenReturn(newUgs);
    when(userRepository.findByChatId(chatId)).thenReturn(user);

    userService.updateUser(chatId, steamId, locale);
    verify(userRepository, times(1)).save(any());
    assertFalse(newUgs.isOwned());
    assertTrue(newUgs.isWished());
  }

  @Test
  void updateSetState_whenGameOwnedButNotWished() throws IOException {
    Game game = new Game();
    UserGameState newUgs = new UserGameState();
    newUgs.setGame(game);
    newUgs.setOwned(true);
    newUgs.setWished(false);
    UserGameState oldUgs = new UserGameState();
    oldUgs.setGame(game);
    oldUgs.setOwned(false);
    oldUgs.setWished(false);

    User user = new User();
    user.setChatId(chatId);
    user.setStates(new HashSet<>(Set.of(oldUgs)));

    when(userGameStateService.getSetStatesByUser(user)).thenReturn(new HashSet<>(Set.of(newUgs)));
    when(userGameStateService.existsByUserAndGame(newUgs.getUser(), newUgs.getGame())).thenReturn(true);
    when(userGameStateService.findByUserAndGame(newUgs.getUser(), newUgs.getGame())).thenReturn(newUgs);
    when(userRepository.findByChatId(chatId)).thenReturn(user);

    userService.updateUser(chatId, steamId, locale);
    verify(userRepository, times(1)).save(any());
    assertTrue(newUgs.isOwned());
    assertFalse(newUgs.isWished());
  }

  @Test
  void updateSetState_whenGameIsNotOwnedAndNotWished() throws IOException {
    Game game = new Game();
    UserGameState newUgs = new UserGameState();
    newUgs.setGame(game);
    newUgs.setOwned(false);
    newUgs.setWished(false);
    UserGameState oldUgs = new UserGameState();
    oldUgs.setGame(game);
    oldUgs.setOwned(false);
    oldUgs.setWished(false);

    User user = new User();
    user.setChatId(chatId);
    user.setStates(new HashSet<>(Set.of(oldUgs)));

    when(userGameStateService.getSetStatesByUser(user)).thenReturn(new HashSet<>(Set.of(newUgs)));
    when(userGameStateService.existsByUserAndGame(newUgs.getUser(), newUgs.getGame())).thenReturn(true);
    when(userGameStateService.findByUserAndGame(newUgs.getUser(), newUgs.getGame())).thenReturn(newUgs);
    when(userRepository.findByChatId(chatId)).thenReturn(user);

    userService.updateUser(chatId, steamId, locale);
    verify(userRepository, times(1)).save(any());
    assertFalse(newUgs.isOwned());
    assertFalse(newUgs.isWished());
  }

  @Test
  void getCountWishedGames() {
    long expectedCount = 10L;
    when(userRepository.countByChatIdAndStates_IsWishedTrue(chatId)).thenReturn(expectedCount);

    assertEquals(expectedCount, userService.getCountWishedGames(chatId));
  }

  @Test
  void getListUsername() {
    List<String> expectedUsernames = List.of("name_1", "name_2");
    when(userRepository.getListUsername()).thenReturn(expectedUsernames);

    List<String> actualList = userService.getListUsername();

    assertEquals(expectedUsernames.size(), actualList.size());
  }
}