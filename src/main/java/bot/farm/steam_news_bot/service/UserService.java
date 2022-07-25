package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SteamService steamService;

    public UserService(UserRepository userRepository, SteamService steamService) {
        this.userRepository = userRepository;
        this.steamService = steamService;
    }

    public void saveOrUpdateUserInDb(String chatId, String name, String steamId) {
        if (userRepository.findUserByChatId(chatId).isPresent())
            userRepository.delete(userRepository.findUserByChatId(chatId).get());

        User user = new User();
        user.setChatId(chatId);
        user.setName(name);
        user.setSteamId(Long.valueOf(steamId));
        user.setGames(steamService.getOwnedGames(user.getSteamId()));

        userRepository.save(user);
    }

    public Optional<User> findUserByChatId(String chatId) {
        return userRepository.findUserByChatId(chatId);
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(users::add);
        return users;
    }

    public List<User> getUsersByAppid(String appid) {
        return new ArrayList<>(userRepository.findByGames_Appid(appid));
    }


}
