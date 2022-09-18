package bot.farm.steam_news_bot.controller;

import bot.farm.steam_news_bot.dto.Message;
import bot.farm.steam_news_bot.service.AnnouncementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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