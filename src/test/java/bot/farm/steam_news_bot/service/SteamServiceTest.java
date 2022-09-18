package bot.farm.steam_news_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SteamServiceTest {
    private final SteamService steamService = new SteamService();
    private Long steamId;
    private String appid;

    @BeforeEach
    void setUp() {
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
        assertNotNull(steamService.getNewsByOwnedGames(appid));
    }

    @Test
    void getWishListGames() {
        assertNotNull(steamService.getWishListGames(steamId));
    }

    @Test
    void isValidSteamId() {
        assertTrue(SteamService.isValidSteamId(String.valueOf(steamId)));
    }
}