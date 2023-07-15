package bot.farm.steam_news_bot.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class UserGameStateTest {
  private UserGameState userGameState;
  private final Long id = 100L;
  @Mock
  private User user;
  @Mock
  private Game game;

  @BeforeEach
  void setUp() {
    userGameState = new UserGameState();


    userGameState.setUser(user);
    userGameState.setGame(game);
    userGameState.setId(id);
    userGameState.setWished(false);
    userGameState.setBanned(false);
    userGameState.setOwned(true);
  }

  @Test
  void testEquals() {
    assertNotEquals(userGameState, new UserGameState());
  }

  @Test
  void testHashCode() {
    assertEquals(Objects.hashCode(userGameState), userGameState.hashCode());
  }

  @Test
  void getId() {
    assertEquals(id, userGameState.getId());
  }

  @Test
  void getUser() {
    assertEquals(user, userGameState.getUser());
  }

  @Test
  void getGame() {
    assertEquals(game, userGameState.getGame());
  }

  @Test
  void isWished() {
    assertFalse(userGameState.isWished());
  }

  @Test
  void isBanned() {
    assertFalse(userGameState.isBanned());
  }

  @Test
  void isOwned() {
    assertTrue(userGameState.isOwned());
  }

}