package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private GameService gameService;
    @InjectMocks
    private MetricsService metricsService;

    @Test
    void getCountUsers() {
        List<User> users = mock(List.class);
        doReturn(users).when(userService).getAllUsers();
        when(users.size()).thenReturn(10);
        doReturn(5L).when(userService).countUsersByActive(true);
        assertEquals(ResponseEntity.ok(Map.of("total users in database", 10,
                "active users", 5)).toString(), metricsService.getCountUsers().toString());
    }

    @Test
    void getCountGames() {
        doReturn(10L).when(gameService).countAllGames();
        doReturn(5L).when(gameService).countByUsersActive();
        assertEquals(ResponseEntity.ok(Map.of("total games in database", 10,
                "game states by active users", 5)).toString(), metricsService.getCountGames().toString());
    }
}