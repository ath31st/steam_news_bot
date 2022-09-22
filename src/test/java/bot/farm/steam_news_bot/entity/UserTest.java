package bot.farm.steam_news_bot.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;
    private final String chatId = "160";
    private final String name = "testName";
    private final String locale = "ru";
    private final Long steamId = 100L;
    private final boolean isActive = true;
    @Mock
    private Set<UserGameState> stateSet;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setSteamId(steamId);
        user.setLocale(locale);
        user.setActive(isActive);
        user.setName(name);
        user.setStates(stateSet);
        user.setChatId(chatId);
    }

    @Test
    void testEquals() {
        assertNotEquals(user, new User());
    }

    @Test
    void testHashCode() {
        assertEquals(Objects.hashCode(user), user.hashCode());
    }

    @Test
    void getChatId() {
        assertEquals(chatId, user.getChatId());
    }

    @Test
    void getName() {
        assertEquals(name, user.getName());
    }

    @Test
    void getSteamId() {
        assertEquals(steamId, user.getSteamId());
    }

    @Test
    void getLocale() {
        assertEquals(locale, user.getLocale());
    }

    @Test
    void isActive() {
        assertEquals(isActive, user.isActive());
    }

    @Test
    void getStates() {
        assertEquals(stateSet, user.getStates());
    }
}