package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.SteamNewsBot;
import bot.farm.steam_news_bot.dto.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private SteamNewsBot steamNewsBot;
    @InjectMocks
    private AnnouncementService announcementService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void receiveMessageAndNotificationUsers() {
        Message message = new Message();
        message.setText("testing test");
        message.setAuthor("Test");

        announcementService.receiveMessageAndNotificationUsers(message);
        verify(userService, times(1)).getUsersByActive(true);
    }
}