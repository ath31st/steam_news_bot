package bot.farm.snb.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
  void testEqualsWithSameInstance() {
    UserGameState gameState = new UserGameState();
    assertEquals(gameState, gameState);
  }

  @Test
  void testEqualsWithNull() {
    UserGameState gameState = new UserGameState();
    assertNotNull(gameState);
  }

  @Test
  void testEqualsWithNullUgs() {
    UserGameState gameState1 = new UserGameState();
    assertFalse(gameState1.equals(null));
  }

  @Test
  void testEqualsWithOtherClassObj() {
    UserGameState gameState1 = new UserGameState();
    Game game1 = new Game();
    assertFalse(gameState1.equals(game1));
  }

  @Test
  void testEqualsWithDifferentClass() {
    UserGameState gameState = new UserGameState();
    Object otherObject = new Object();
    assertNotEquals(gameState, otherObject);
  }

  @Test
  void testEqualsWithDifferentId() {
    UserGameState gameState1 = new UserGameState();
    UserGameState gameState2 = new UserGameState();
    gameState1.setId(1L);
    gameState2.setId(2L);
    assertNotEquals(gameState1, gameState2);
  }

  @Test
  void testEqualsWithDifferentUser() {
    UserGameState gameState1 = new UserGameState();
    UserGameState gameState2 = new UserGameState();
    User u1 = new User();
    u1.setChatId("1");
    User u2 = new User();
    u2.setChatId("2");
    gameState1.setUser(u1);
    gameState2.setUser(u2);
    assertNotEquals(gameState1, gameState2);
  }

  @Test
  void testEqualsWithDifferentGame() {
    UserGameState gameState1 = new UserGameState();
    UserGameState gameState2 = new UserGameState();
    Game g1 = new Game();
    g1.setAppid("1");
    Game g2 = new Game();
    g2.setAppid("2");
    gameState1.setGame(g1);
    gameState2.setGame(g2);
    assertNotEquals(gameState1, gameState2);
  }

  @Test
  void testEqualsWithEqualObjects() {
    UserGameState gameState1 = new UserGameState();
    UserGameState gameState2 = new UserGameState();

    User u1 = new User();
    u1.setChatId("1");
    Game g1 = new Game();
    g1.setAppid("1");

    gameState1.setId(1L);
    gameState2.setId(1L);
    gameState1.setUser(u1);
    gameState2.setUser(u1);
    gameState1.setGame(g1);
    gameState2.setGame(g1);
    assertEquals(gameState1, gameState2);
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