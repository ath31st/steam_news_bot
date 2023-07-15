package bot.farm.steam_news_bot.config;

import bot.farm.steam_news_bot.SteamNewsBot;
import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.SteamService;
import bot.farm.steam_news_bot.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SchedulerConfigTest {
  @Mock
  private SteamNewsBot steamNewsBot;
  @Mock
  private SteamService steamService;
  @Mock
  private UserService userService;
  @Mock
  private GameService gameService;
  @InjectMocks
  private SchedulerConfig schedulerConfig;

  @Test
  void testTask1() {
    ReflectionTestUtils.invokeMethod(schedulerConfig, "updateAndSendNewsItems");
  }

  @Test
  void testTask2() {
    ReflectionTestUtils.invokeMethod(schedulerConfig, "processingProblemGame");
  }
}
