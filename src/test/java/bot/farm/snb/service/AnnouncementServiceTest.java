package bot.farm.snb.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bot.farm.snb.SteamNewsBot;
import bot.farm.snb.dto.Message;
import bot.farm.snb.entity.User;
import java.util.List;
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
    User user = new User();
    user.setChatId("1");
    Message message = new Message();
    message.setText("testing test");
    message.setAuthor("Test");

    when(userService.getUsersByActive(true)).thenReturn(List.of(user));

    announcementService.receiveMessageAndNotificationUsers(message);
    verify(userService, times(1)).getUsersByActive(true);
  }
}