package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.NewsItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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

    @Test
    void getOwnedGames() throws IOException {
        String steamwebapikey = System.getenv("steamWebApiKey");
        ReflectionTestUtils.setField(steamService, "steamWebApiKey", steamwebapikey);
        assertNotNull(steamService.getOwnedGames(steamId));
        assertThrows(NullPointerException.class, () -> steamService.getOwnedGames(76561198150389652L));
    }

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
}