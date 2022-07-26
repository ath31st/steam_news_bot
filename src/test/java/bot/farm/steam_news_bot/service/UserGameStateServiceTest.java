package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import bot.farm.steam_news_bot.repository.UserGameStateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGameStateServiceTest {

    @Mock
    private UserGameStateRepository userGameStateRepository;
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
}