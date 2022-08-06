package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import bot.farm.steam_news_bot.repository.UserGameStateRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserGameStateService {
    private final UserGameStateRepository userGameStateRepository;

    public UserGameStateService(UserGameStateRepository userGameStateRepository) {
        this.userGameStateRepository = userGameStateRepository;
    }

    public void updateStateForGameByChatId(String chatId, String name, boolean isBanned) {
        UserGameState userGameState = userGameStateRepository.findByUser_ChatIdAndGame_Name(chatId, name);
        userGameStateRepository.updateIsBannedById(isBanned, userGameState.getId());
    }

    public void updateStateForGameById(boolean isBanned, Long id) {
        userGameStateRepository.updateIsBannedById(isBanned, id);
    }

    public Set<UserGameState> getBlackListByChatId(String chatId) {
        return userGameStateRepository.findByUser_ChatIdAndIsBannedTrue(chatId);
    }

    public void deleteByUser(User user) {
        userGameStateRepository.deleteByUser(user);
    }
}
