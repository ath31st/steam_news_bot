package bot.farm.snb.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class GameTest {
  private Game game;
  private final String appid = "440";
  private final String name = "testGame";
  @Mock
  private Set<UserGameState> states;

  @BeforeEach
  void setUp() {
    game = new Game();
    game.setAppid(appid);
    game.setName(name);
    game.setStates(states);
  }

  @Test
  void testEquals() {
    assertNotEquals(game, new Game());
  }

  @Test
  void testHashCode() {
    assertEquals(Objects.hashCode(game), game.hashCode());
  }

  @Test
  void getAppid() {
    assertEquals(appid, game.getAppid());
  }

  @Test
  void getName() {
    assertEquals(name, game.getName());
  }

  @Test
  void getStates() {
    assertEquals(states, game.getStates());
  }

}