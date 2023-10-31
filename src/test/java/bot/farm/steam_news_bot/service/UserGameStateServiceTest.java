package bot.farm.steam_news_bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import bot.farm.steam_news_bot.repository.UserGameStateRepository;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserGameStateServiceTest {

  @Mock
  private UserGameStateRepository userGameStateRepository;
  @Mock
  private SteamService steamService;
  @InjectMocks
  private UserGameStateService userGameStateService;
  private final UserGameState userGameState = mock(UserGameState.class);

  private Long id;
  private String chatId;
  private String name;
  private boolean isBanned;
  private boolean isWished;
  private boolean isOwned;

  @BeforeEach
  void setUp() {
    id = 1L;
    chatId = "160";
    name = "Test";
    isBanned = false;
    isWished = false;
    isOwned = true;
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void updateStateForGameByChatId() {
    doReturn(userGameState).when(userGameStateRepository).findByUser_ChatIdAndGame_Name(chatId, name);
    doReturn(1L).when(userGameState).getId();
    userGameStateService.updateStateForGameByChatId(chatId, name, isBanned);
    verify(userGameStateRepository, times(1)).findByUser_ChatIdAndGame_Name(chatId, name);
    verify(userGameStateRepository, times(1)).updateIsBannedById(isBanned, userGameState.getId());
  }

  @Test
  void updateStateForGameById() {
    userGameStateService.updateStateForGameById(isBanned, id);
    verify(userGameStateRepository).updateIsBannedById(isBanned, id);
  }

  @Test
  void testUpdateStateForGameById() {
    userGameStateService.updateStateForGameById(isWished, isOwned, id);
    verify(userGameStateRepository).updateIsWishedAndIsOwnedById(isWished, isOwned, id);
  }

  @Test
  void getBlackListByChatId() {
    assertEquals(0, userGameStateService.getBlackListByChatId(chatId).size());
  }

  @Test
  void existsByUserAndGame() {
    User user = mock(User.class);
    Game game = mock(Game.class);
    assertFalse(userGameStateService.existsByUserAndGame(user, game));
  }

  @Test
  void findByUserAndGame() {
    User user = mock(User.class);
    Game game = mock(Game.class);
    UserGameState userGameState1 = userGameStateService.findByUserAndGame(user, game);
    assertNull(userGameState1);
  }

  @Test
  void getTopGamesFromDb() {
    int limit = 2;
    List<String> expectedList = List.of("game_1", "game_2");

    when(userGameStateRepository.findTopGames(limit)).thenReturn(expectedList);

    assertEquals(expectedList.size(), userGameStateService.getTopGamesFromDb(limit).size());
  }

  @Test
  void getSetStatesByUser() throws IOException {
    User user = new User();
    user.setSteamId(123456789L);
    Game ownedGame1 = new Game();
    ownedGame1.setAppid("1");
    Game ownedGame2 = new Game();
    ownedGame2.setAppid("2");
    Game wishedGame1 = new Game();
    wishedGame1.setAppid("3");
    Game wishedGame2 = new Game();
    wishedGame2.setAppid("4");
    List<Game> games = List.of(ownedGame1, ownedGame2, wishedGame1, wishedGame2);

    when(steamService.getOwnedGames(user.getSteamId())).thenReturn(List.of(ownedGame1, ownedGame2));
    when(steamService.getWishListGames(user.getSteamId())).thenReturn(List.of(wishedGame1, wishedGame2));

    Set<UserGameState> userGameStates = userGameStateService.getSetStatesByUser(user);
    assertEquals(games.size(), userGameStates.size());
  }
}
