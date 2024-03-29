package bot.farm.snb.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import bot.farm.snb.dto.Message;
import bot.farm.snb.service.AnnouncementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnouncementControllerTest {
  @Mock
  private AnnouncementService announcementService;
  @InjectMocks
  private AnnouncementController announcementController;

  @Test
  void receiveMessage() {
    Message message = new Message();
    message.setText("test");
    message.setAuthor("author");

    announcementController.receiveMessage(message);
    verify(announcementService, times(1)).receiveMessageAndNotificationUsers(message);
  }
}