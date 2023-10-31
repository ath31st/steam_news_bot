package bot.farm.steam_news_bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.NewsItem;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SteamServiceTest {
  private final SteamService steamService = new SteamService();
  private Long steamId;
  private String appid;
  private HttpURLConnection connection;

  @BeforeEach
  void setUp() {
    connection = mock(HttpURLConnection.class);
    appid = "440";
    steamId = 76561198276100918L;
  }

//  @Test
//  void getOwnedGames() throws IOException {
//    String steamwebapikey = System.getenv("steamnewsbot.steamWebApiKey");
//    ReflectionTestUtils.setField(steamService, "steamWebApiKey", steamwebapikey);
//    assertNotNull(steamService.getOwnedGames(steamId));
//    assertThrows(NullPointerException.class, () -> steamService.getOwnedGames(76561198150389652L));
//  }

  @Test
  void getNewsByOwnedGames() throws IOException {
    List<NewsItem> list = steamService.getNewsByOwnedGames(appid);
    assertNotNull(list);
  }

  @Test
  void getWishListGames() {
    List<Game> list = steamService.getWishListGames(steamId);
    assertNotNull(list);
  }

  @Test
  void isValidSteamId() {
    assertTrue(SteamService.isValidSteamId(String.valueOf(steamId)));
  }

  @Test
  void checkAvailableWishlistBySteamId_throwingException() {
    long steamId = 123456789L;

    int responseCode = steamService.checkAvailableWishlistBySteamId(steamId);

    assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, responseCode);
  }
}
