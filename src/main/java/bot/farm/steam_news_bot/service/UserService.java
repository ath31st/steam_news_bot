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

@Service
public class UserService {
  private final UserRepository userRepository;
  private final UserGameStateService userGameStateService;
  private final SteamService steamService;
  
  public UserService(UserRepository userRepository,
                     UserGameStateService userGameStateService,
                     SteamService steamService) {
    this.userRepository = userRepository;
    this.userGameStateService = userGameStateService;
    this.steamService = steamService;
  }
  
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
  
  public void updateUser(String chatId,
                         String steamId,
                         String locale) throws IOException, NullPointerException {
    if (userRepository.findUserByChatId(chatId).isPresent()) {
      User user = userRepository.findUserByChatId(chatId).get();
      user.setSteamId(Long.valueOf(steamId));
      user.setLocale(locale);
      Set<UserGameState> states = updateSetStates(getSetStatesByUser(user));
      user.setStates(states);
      
      userRepository.save(user);
    }
  }
  
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
  
  public long getCountOwnedGames(String chatId) {
    return userRepository.countByChatIdAndStates_IsOwnedTrue(chatId);
  }
  
  public void updateActiveForUser(String chatId, boolean active) {
    if (userRepository.findUserByChatId(chatId).isPresent()) {
      userRepository.updateActiveByChatId(active, chatId);
    }
  }
  
  public Optional<User> findUserByChatId(String chatId) {
    return userRepository.findUserByChatId(chatId);
  }
  
  public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    userRepository.findAll().iterator().forEachRemaining(users::add);
    return users;
  }
  
  public Set<User> getUsersWithFilters(String appid) {
    return userRepository.findByActiveTrueAndStates_Game_AppidAndStates_IsBannedFalse(appid);
  }
  
  public List<User> getUsersByActive(boolean isActive) {
    return userRepository.findByActive(isActive);
  }
  
  public long countUsersByActive(boolean active) {
    return userRepository.countByActive(active);
  }
  
  public boolean existsByChatId(String chatId) {
    return userRepository.existsByChatId(chatId);
  }
  
  public User getUserByChatId(String chatId) {
    return userRepository.findByChatId(chatId);
  }
  
  public boolean checkBanForGameByChatId(String chatId, String name) {
    return userRepository.existsByChatIdAndStates_Game_NameAndStates_IsBannedTrue(chatId, name);
  }
  
  public List<String> getListUsername() {
    return userRepository.getListUsername();
  }
  
}
