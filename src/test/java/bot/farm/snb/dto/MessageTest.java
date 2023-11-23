package bot.farm.snb.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void testEqualsWithSameObject() {
    Message message = new Message();
    assertTrue(message.equals(message));
  }

  @Test
  void testEqualsWithEqualObjects() {
    Message message1 = new Message();
    Message message2 = new Message();

    message1.setAuthor("author");
    message1.setText("Hello, world!");
    message1.setDate("2023-11-08");

    message2.setAuthor("author");
    message2.setText("Hello, world!");
    message2.setDate("2023-11-08");

    assertTrue(message1.equals(message2));
  }

  @Test
  void testEqualsWithDifferentAuthor() {
    Message message1 = new Message();
    Message message2 = new Message();

    message1.setAuthor("author");
    message1.setText("Hello, world!");

    message2.setAuthor("another_author");
    message2.setText("Hello, world!");

    assertFalse(message1.equals(message2));
  }

  @Test
  void testEqualsWithDifferentText() {
    Message message1 = new Message();
    Message message2 = new Message();

    message1.setAuthor("author");
    message1.setText("Hello, world!");

    message2.setAuthor("author");
    message2.setText("Good morning!");

    assertFalse(message1.equals(message2));
  }

  @Test
  void testEqualsWithNull() {
    Message message1 = new Message();
    assertFalse(message1.equals(null));
  }

  @Test
  void testEqualsWithOtherClass() {
    Message message1 = new Message();
    assertFalse(message1.equals("woop"));
  }

  @Test
  void testEqualsWithEqualHashCode() {
    Message message1 = new Message();
    Message message2 = new Message();

    message1.setAuthor("author");
    message1.setText("Hello, world!");
    message1.setDate("2023-11-08");

    message2.setAuthor("author");
    message2.setText("Hello, world!");
    message2.setDate("2023-11-08");

    assertEquals(message1.hashCode(), message2.hashCode());
  }
}