package bot.farm.steam_news_bot.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageTest {
  private Message message;
  private final String date = "12.12.2022";
  private final String author = "author";
  private final String text = "test";

  @BeforeEach
  void setup() {
    message = new Message();
    message.setDate(date);
    message.setAuthor(author);
    message.setText(text);
  }

  @Test
  void getDate() {
    assertEquals(date, message.getDate());
  }

  @Test
  void getAuthor() {
    assertEquals(author, message.getAuthor());
  }

  @Test
  void getText() {
    assertEquals(text, message.getText());
  }

  @Test
  void testToString() {
    assertEquals("From/От author" + System.lineSeparator() +
        "<b>12.12.2022</b>" + System.lineSeparator() +
        "Announcement!/Объявление! test" + System.lineSeparator() +
        "Thank for your attention!/Благодарю за внимание!", message.toString());
  }
}