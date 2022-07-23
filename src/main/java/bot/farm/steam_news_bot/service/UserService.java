package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveOrUpdateUserInDb(User user) {
        userRepository.save(user);
    }

    public Optional<User> findUserByChatId(String chatId) {
        return userRepository.findUserByChatId(chatId);
    }

}
