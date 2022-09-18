package bot.farm.steam_news_bot.controller;

import bot.farm.steam_news_bot.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {
    @Mock
    private MetricsService metricsService;
    @InjectMocks
    private MetricsController metricsController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getCountUsers() {
        metricsController.getCountUsers();
        verify(metricsService, times(1)).getCountUsers();
    }

    @Test
    void getCountGames() {
        metricsController.getCountGames();
        verify(metricsService, times(1)).getCountGames();
    }
}