package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import bot.farm.steam_news_bot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SteamService steamService;

    public UserService(UserRepository userRepository, SteamService steamService) {
        this.userRepository = userRepository;
        this.steamService = steamService;
    }

    public void saveOrUpdateUserInDb(String chatId, String name, String steamId) {
        if (userRepository.findUserByChatId(chatId).isEmpty()) {

            User user = new User();
            user.setActive(true);
            user.setChatId(chatId);
            user.setName(name);
            user.setSteamId(Long.valueOf(steamId));
            user.setStates(getSetStatesByUser(user));

            userRepository.save(user);

        } else {
            User user = userRepository.findUserByChatId(chatId).get();
            user.setSteamId(Long.valueOf(steamId));
            user.setStates(getSetStatesByUser(user));

            userRepository.save(user);
        }
    }
    private Set<UserGameState> getSetStatesByUser(User user){
        return steamService.getOwnedGames(user.getSteamId())
                .stream()
                .parallel()
                .map(game -> {
                    UserGameState userGameState = new UserGameState();
                    userGameState.setUser(user);
                    userGameState.setGame(game);
                    userGameState.setBanned(false);
                    userGameState.setWished(false);
                    return userGameState;
                })
                .collect(Collectors.toSet());
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

    public List<User> getUsersWithFilters(String appid) {
        return new ArrayList<>(userRepository.findByActiveTrueAndStates_Game_AppidAndStates_IsBannedFalse(appid));
    }

    public List<User> getUsersByActive(boolean isActive) {
        return new ArrayList<>(userRepository.findByActive(isActive));
    }

}
