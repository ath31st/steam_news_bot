package bot.farm.steam_news_bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import bot.farm.steam_news_bot.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

  @Mock
  private GameRepository gameRepository;
  @InjectMocks
  private GameService gameService;

  @Test
  void getBanListByChatId() {
    String chatId = "1";
    gameService.getBanListByChatId(chatId);
    verify(gameRepository, times(1)).findByStates_User_ChatIdAndStates_IsBannedTrue(chatId);
  }

  @Test
  void getAllGamesByActiveUsers() {
    assertEquals(0, gameService.getAllGamesByActiveUsers().size());
  }

  @Test
  void countAllGames() {
    doReturn(10L).when(gameRepository).count();
    assertEquals(10L, gameService.countAllGames());
  }

  @Test
  void countByUsersActive() {
    doReturn(10L).when(gameRepository).countByStates_User_ActiveTrue();
    assertEquals(10L, gameService.countByUsersActive());
  }
}