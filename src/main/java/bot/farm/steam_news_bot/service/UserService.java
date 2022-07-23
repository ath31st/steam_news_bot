package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUserInDb(User user) {
        if (userRepository.findUserByChatId(user.getChatId()).isEmpty()) {
            userRepository.save(user);
            System.out.println("Saved new user: " + user.getChatId());
        } else {
            System.out.println("User already exists");
        }

    }

}
