package bot.farm.steam_news_bot.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import bot.farm.steam_news_bot.SteamNewsBot;
import bot.farm.steam_news_bot.dto.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {
  @Mock
  private UserService userService;
  @Mock
  private SteamNewsBot steamNewsBot;
  @InjectMocks
  private AnnouncementService announcementService;

  @Test
  void receiveMessageAndNotificationUsers() {
    Message message = new Message();
    message.setText("testing test");
    message.setAuthor("Test");

    announcementService.receiveMessageAndNotificationUsers(message);
    verify(userService, times(1)).getUsersByActive(true);
  }
}