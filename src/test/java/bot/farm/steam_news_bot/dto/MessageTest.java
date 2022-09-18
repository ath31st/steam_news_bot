package bot.farm.steam_news_bot.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testToString() {
        Message message = new Message();
        message.setDate("12.12.2022");
        message.setAuthor("author");
        message.setText("test");

        assertEquals("From author" + System.lineSeparator() +
                "<b>12.12.2022</b>" + System.lineSeparator() +
                "Announcement! test" + System.lineSeparator() +
                "Thank you for your attention my friend!",message.toString());
    }
}