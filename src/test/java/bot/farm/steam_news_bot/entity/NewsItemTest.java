package bot.farm.steam_news_bot.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NewsItemTest {

  NewsItem newsItem;

  @BeforeEach
  void setUp() {
    newsItem = new NewsItem();
    newsItem.setContents("content text");
    newsItem.setDate(String.valueOf(LocalDateTime.of(2012, 12, 12, 12, 12)));
    newsItem.setAppid("440");
    newsItem.setAuthor("author");
    newsItem.setGid(156664);
    newsItem.setUrl("url");
    newsItem.setTitle("test title");
    newsItem.setExternalUrl(true);
    newsItem.setFeedLabel("test label");
    newsItem.setFeedName("test name");
    newsItem.setFeedType(1);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testToString() {
    assertEquals("<b>2012-12-12T12:12</b>" + System.lineSeparator() +
        "<b>test title</b>" + System.lineSeparator() +
        "content text" + System.lineSeparator() +
        "<a href=\"url\">LINK(440)</a>", newsItem.toString());
  }

  @Test
  void getGid() {
    assertEquals(156664, newsItem.getGid());
  }

  @Test
  void getTitle() {
    assertEquals("test title", newsItem.getTitle());
  }

  @Test
  void getUrl() {
    assertEquals("url", newsItem.getUrl());
  }

  @Test
  void getAuthor() {
    assertEquals("author", newsItem.getAuthor());
  }

  @Test
  void getContents() {
    assertEquals("content text", newsItem.getContents());
  }

  @Test
  void isExternalUrl() {
    assertTrue(newsItem.isExternalUrl());
  }

  @Test
  void getFeedLabel() {
    assertEquals("test label", newsItem.getFeedLabel());
  }

  @Test
  void getDate() {
    assertEquals("2012-12-12T12:12", newsItem.getDate());
  }

  @Test
  void getFeedName() {
    assertEquals("test name", newsItem.getFeedName());
  }

  @Test
  void getFeedType() {
    assertEquals(1, newsItem.getFeedType());
  }

  @Test
  void getAppid() {
    assertEquals("440", newsItem.getAppid());
  }

  @Test
  void setGid() {
    newsItem.setGid(111222);
    assertEquals(111222, newsItem.getGid());
  }

  @Test
  void setTitle() {
    newsItem.setTitle("new title");
    assertEquals("new title", newsItem.getTitle());
  }

  @Test
  void setUrl() {
    newsItem.setUrl("new url");
    assertEquals("new url", newsItem.getUrl());
  }

  @Test
  void setAuthor() {
    newsItem.setAuthor("new author");
    assertEquals("new author", newsItem.getAuthor());
  }

  @Test
  void setContents() {
    newsItem.setContents("new content");
    assertEquals("new content", newsItem.getContents());
  }

  @Test
  void setExternalUrl() {
    newsItem.setExternalUrl(false);
    assertFalse(newsItem.isExternalUrl());
  }

  @Test
  void setFeedLabel() {
    newsItem.setFeedLabel("new label");
    assertEquals("new label", newsItem.getFeedLabel());
  }

  @Test
  void setDate() {
    newsItem.setDate(String.valueOf(LocalDateTime.of(2011, 11, 11, 11, 11)));
    assertEquals("2011-11-11T11:11", newsItem.getDate());
  }

  @Test
  void setFeedName() {
    newsItem.setFeedName("new name");
    assertEquals("new name", newsItem.getFeedName());
  }

  @Test
  void setFeedType() {
    newsItem.setFeedType(2);
    assertEquals(2, newsItem.getFeedType());
  }

  @Test
  void setAppid() {
    newsItem.setAppid("330");
    assertEquals("330", newsItem.getAppid());
  }
}