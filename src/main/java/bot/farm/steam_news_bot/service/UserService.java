package bot.farm.steam_news_bot.service;

import static bot.farm.steam_news_bot.localization.message.MessageEnum.DEFAULT_NAME;
import static bot.farm.steam_news_bot.localization.message.MessageLocalization.getMessage;

import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import bot.farm.steam_news_bot.repository.UserRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Service class for managing user-related operations and interactions.
 */
@Service
public class UserService {
  private final UserRepository userRepository;
  private final UserGameStateService userGameStateService;
  private final SteamService steamService;

  /**
   * Constructs a new UserService with the provided dependencies.
   *
   * @param userRepository       the UserRepository to be used
   * @param userGameStateService the UserGameStateService to be used
   * @param steamService         the SteamService to be used
   */
  public UserService(UserRepository userRepository,
                     UserGameStateService userGameStateService,
                     SteamService steamService) {
    this.userRepository = userRepository;
    this.userGameStateService = userGameStateService;
    this.steamService = steamService;
  }

  /**
   * Saves a new user with the provided information.
   *
   * @param chatId  the chat ID of the user
   * @param name    the name of the user (can be null)
   * @param steamId the Steam ID of the user
   * @param locale  the locale of the user
   * @throws IOException          if there is an error during the operation
   * @throws NullPointerException if any of the required parameters are null
   */
  public void saveUser(String chatId,
                       String name,
                       String steamId,
                       String locale) throws IOException, NullPointerException {
    if (userRepository.findUserByChatId(chatId).isPresent()) {
      return;
    }
    if (name == null) {
      name = getMessage(DEFAULT_NAME, locale);
    }

    User user = new User();
    user.setActive(true);
    user.setLocale(locale);
    user.setChatId(chatId);
    user.setName(name);
    user.setSteamId(Long.valueOf(steamId));
    user.setStates(getSetStatesByUser(user));

    userRepository.save(user);

  }

  /**
   * Updates an existing user with the provided information.
   *
   * @param chatId  the chat ID of the user
   * @param steamId the Steam ID of the user
   * @param locale  the locale of the user
   * @throws IOException          if there is an error during the operation
   * @throws NullPointerException if any of the required parameters are null
   */
  public void updateUser(String chatId,
                         String steamId,
                         String locale) throws IOException, NullPointerException {
    if (userRepository.findUserByChatId(chatId).isPresent()) {
      User user = userRepository.findUserByChatId(chatId).orElseThrow();
      user.setSteamId(Long.valueOf(steamId));
      user.setLocale(locale);
      Set<UserGameState> states = updateSetStates(getSetStatesByUser(user));
      user.setStates(states);

      userRepository.save(user);
    }
  }

  /**
   * Retrieves the set of user game states based on the provided user.
   *
   * @param user the User object
   * @return the set of UserGameState objects
   * @throws IOException          if there is an error during the operation
   * @throws NullPointerException if the user parameter is null
   */
  private Set<UserGameState> getSetStatesByUser(User user)
      throws IOException, NullPointerException {
    return steamService.getOwnedGames(user.getSteamId())
        .parallelStream()
        .map(game -> {
          UserGameState userGameState = new UserGameState();
          userGameState.setUser(user);
          userGameState.setGame(game);
          userGameState.setBanned(false);
          userGameState.setWished(false);
          userGameState.setOwned(true);
          return userGameState;
        })
        .collect(Collectors.toSet());
  }

  /**
   * Updates the set of user game states by removing duplicates and retrieving existing states.
   *
   * @param newSet the new set of UserGameState objects
   * @return the updated set of UserGameState objects
   */
  private Set<UserGameState> updateSetStates(Set<UserGameState> newSet) {
    Iterator<UserGameState> iterator = newSet.iterator();
    Set<UserGameState> tmp = new HashSet<>();
    while (iterator.hasNext()) {
      UserGameState ugs = iterator.next();
      if (userGameStateService.existsByUserAndGame(ugs.getUser(), ugs.getGame())) {
        UserGameState oldState =
            userGameStateService.findByUserAndGame(ugs.getUser(), ugs.getGame());
        tmp.add(oldState);
        iterator.remove();
      }
    }

    newSet.addAll(tmp);
    return newSet;
  }

  /**
   * Retrieves the count of owned games for a specific chat ID.
   *
   * @param chatId the chat ID
   * @return the count of owned games
   */
  public long getCountOwnedGames(String chatId) {
    return userRepository.countByChatIdAndStates_IsOwnedTrue(chatId);
  }

  /**
   * Updates the 'active' flag for a user based on the chat ID.
   *
   * @param chatId the chat ID
   * @param active true to set the user as active, false otherwise
   */
  public void updateActiveForUser(String chatId, boolean active) {
    if (userRepository.findUserByChatId(chatId).isPresent()) {
      userRepository.updateActiveByChatId(active, chatId);
    }
  }

  /**
   * Retrieves the user with the specified chat ID, if it exists.
   *
   * @param chatId the chat ID
   * @return an Optional containing the User object, or an empty Optional if not found
   */
  public Optional<User> findUserByChatId(String chatId) {
    return userRepository.findUserByChatId(chatId);
  }

  /**
   * Retrieves all users.
   *
   * @return the list of all User objects
   */
  public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    userRepository.findAll().iterator().forEachRemaining(users::add);
    return users;
  }

  /**
   * Retrieves a set of users with the specified app ID, who are active and not banned for the game.
   *
   * @param appid the app ID of the game
   * @return the set of User objects matching the criteria
   */
  public Set<User> getUsersWithFilters(String appid) {
    return userRepository.findByActiveTrueAndStates_Game_AppidAndStates_IsBannedFalse(appid);
  }

  /**
   * Retrieves a list of users based on their active status.
   *
   * @param isActive true to retrieve active users, false to retrieve inactive users
   * @return the list of User objects matching the active status
   */
  public List<User> getUsersByActive(boolean isActive) {
    return userRepository.findByActive(isActive);
  }

  /**
   * Retrieves the count of users based on their active status.
   *
   * @param active true to count active users, false to count inactive users
   * @return the count of users matching the active status
   */
  public long countUsersByActive(boolean active) {
    return userRepository.countByActive(active);
  }

  /**
   * Checks if a user with the specified chat ID exists.
   *
   * @param chatId the chat ID
   * @return true if a user with the chat ID exists, false otherwise
   */
  public boolean existsByChatId(String chatId) {
    return userRepository.existsByChatId(chatId);
  }

  /**
   * Retrieves the user with the specified chat ID.
   *
   * @param chatId the chat ID
   * @return the User object
   */
  public User getUserByChatId(String chatId) {
    return userRepository.findByChatId(chatId);
  }

  /**
   * Checks if a game is banned for a specific user based on the chat ID and game name.
   *
   * @param chatId the chat ID
   * @param name   the name of the game
   * @return true if the game is banned for the user, false otherwise
   */
  public boolean checkBanForGameByChatId(String chatId, String name) {
    return userRepository.existsByChatIdAndStates_Game_NameAndStates_IsBannedTrue(chatId, name);
  }

  /**
   * Retrieves a list of usernames.
   *
   * @return the list of usernames
   */
  public List<String> getListUsername() {
    return userRepository.getListUsername();
  }
}
