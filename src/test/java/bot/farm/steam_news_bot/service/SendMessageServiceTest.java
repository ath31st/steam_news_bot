package bot.farm.steam_news_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SendMessageServiceTest {
    @Mock
    private ButtonService buttonService;
    @InjectMocks
    private SendMessageService sendMessageService;
    private String chatId;
    private String message;
    private String locale;

    @BeforeEach
    void setUp() {
        chatId = "160";
        message = "test";
        locale = "ru";
    }

    @Test
    void createMessage() {
        assertNotNull(sendMessageService.createMessage(chatId, message));
    }

    @Test
    void createMenuMessage() {
        assertNotNull(sendMessageService.createMenuMessage(chatId, message, locale));
    }

    @Test
    void createNewsMessage() {
        assertNotNull(sendMessageService.createNewsMessage(chatId, message, locale));
    }
}