package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import bot.farm.steam_news_bot.repository.UserGameStateRepository;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user game states and performing operations related to user game
 * states.
 */
@Service
public class UserGameStateService {
  private final UserGameStateRepository userGameStateRepository;
  private final SteamService steamService;

  /**
   * Constructs a new UserGameStateService with the provided UserGameStateRepository.
   *
   * @param userGameStateRepository the UserGameStateRepository to be used
   * @param steamService            the Steam service
   */

  public UserGameStateService(UserGameStateRepository userGameStateRepository, SteamService steamService) {
    this.userGameStateRepository = userGameStateRepository;
    this.steamService = steamService;
  }

  /**
   * Updates the state (banned or not) for a game based on the chat ID and game name.
   *
   * @param chatId   the chat ID
   * @param name     the name of the game
   * @param isBanned true if the game is banned, false otherwise
   */
  public void updateStateForGameByChatId(String chatId, String name, boolean isBanned) {
    UserGameState userGameState =
        userGameStateRepository.findByUser_ChatIdAndGame_Name(chatId, name);
    userGameStateRepository.updateIsBannedById(isBanned, userGameState.getId());
  }

  /**
   * Updates the state (banned or not) for a game based on the game ID.
   *
   * @param isBanned true if the game is banned, false otherwise
   * @param id       the ID of the game
   */
  public void updateStateForGameById(boolean isBanned, Long id) {
    userGameStateRepository.updateIsBannedById(isBanned, id);
  }

  /**
   * Updates the state (wished and owned or not) for a game based on the game ID.
   *
   * @param isWished true if the game is wished, false otherwise
   * @param isOwned  true if the game is owned, false otherwise
   * @param id       the ID of the game
   */
  public void updateStateForGameById(boolean isWished, boolean isOwned, Long id) {
    userGameStateRepository.updateIsWishedAndIsOwnedById(isWished, isOwned, id);
  }

  /**
   * Retrieves the list of blacklisted games for a specific chat ID.
   *
   * @param chatId the chat ID
   * @return the list of blacklisted UserGameState objects
   */
  public List<UserGameState> getBlackListByChatId(String chatId) {
    return userGameStateRepository.findByUser_ChatIdAndIsBannedTrue(chatId);
  }

  /**
   * Checks if a UserGameState entry exists for the given User and Game.
   *
   * @param user the User object
   * @param game the Game object
   * @return true if the UserGameState exists, false otherwise
   */
  public boolean existsByUserAndGame(User user, Game game) {
    return userGameStateRepository.existsByUserAndGame(user, game);
  }

  /**
   * Retrieves the UserGameState entry for the given User and Game.
   *
   * @param user the User object
   * @param game the Game object
   * @return the UserGameState object, or null if not found
   */
  public UserGameState findByUserAndGame(User user, Game game) {
    return userGameStateRepository.findByUserAndGame(user, game);
  }

  /**
   * Retrieves the list of top games from the database based on the specified limit.
   *
   * @param limit the maximum number of games to retrieve
   * @return the list of top games
   */
  public List<String> getTopGamesFromDb(int limit) {
    return userGameStateRepository.findTopGames(limit);
  }

  /**
   * Retrieves the set of user game states based on the provided user.
   *
   * @param user the User object
   * @return the set of UserGameState objects
   * @throws IOException          if there is an error during the operation
   * @throws NullPointerException if the user parameter is null
   */
  public Set<UserGameState> getSetStatesByUser(User user)
      throws IOException, NullPointerException {
    final Set<UserGameState> ownedGames = steamService.getOwnedGames(user.getSteamId())
        .parallelStream()
        .map(game -> {
          UserGameState ugs = new UserGameState();
          ugs.setUser(user);
          ugs.setGame(game);
          ugs.setBanned(false);
          ugs.setWished(false);
          ugs.setOwned(true);
          return ugs;
        })
        .collect(Collectors.toSet());

    final Set<UserGameState> wishedGames = steamService.getWishListGames(user.getSteamId())
        .parallelStream()
        .map(game -> {
          UserGameState ugs = new UserGameState();
          ugs.setUser(user);
          ugs.setGame(game);
          ugs.setBanned(false);
          ugs.setWished(true);
          ugs.setOwned(false);
          return ugs;
        })
        .collect(Collectors.toSet());

    ownedGames.addAll(wishedGames);
    return ownedGames;
  }
}
