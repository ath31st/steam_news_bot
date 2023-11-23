package bot.farm.snb.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import bot.farm.snb.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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