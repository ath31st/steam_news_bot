package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.repository.GameRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Service class for handling game-related operations.
 */
@Service
public class GameService {
  private final GameRepository gameRepository;

  /**
   * Constructs a new GameService with the given GameRepository.
   *
   * @param gameRepository The GameRepository used for accessing game data.
   */
  public GameService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  /**
   * Retrieves the list of banned games for a specific chat ID.
   *
   * @param chatId The chat ID for which to retrieve the ban list.
   * @return The ban list as a string, with game names separated by newline characters.
   */
  public String getBanListByChatId(String chatId) {
    List<Game> games = gameRepository.findByStates_User_ChatIdAndStates_IsBannedTrue(chatId);
    return games.stream()
        .map(Game::getName)
        .collect(Collectors.joining("\n"));
  }

  /**
   * Retrieves all games associated with active users, excluding banned games.
   *
   * @return The set of games associated with active users.
   */
  public Set<Game> getAllGamesByActiveUsers() {
    return gameRepository.findByStates_User_ActiveTrueAndStates_IsBannedFalse();
  }

  /**
   * Counts the total number of games.
   *
   * @return The total number of games.
   */
  public long countAllGames() {
    return gameRepository.count();
  }

  /**
   * Counts the number of games associated with active users.
   *
   * @return The number of games associated with active users.
   */
  public long countByUsersActive() {
    return gameRepository.countByStates_User_ActiveTrue();
  }
}
