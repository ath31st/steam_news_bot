package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public String getBanListByChatId(String chatId) {
        List<Game> games = gameRepository.findByStates_User_ChatIdAndStates_IsBannedTrue(chatId);
        return games.stream()
                .map(Game::getName)
                .collect(Collectors.joining("\n"));
    }

    public Set<Game> getAllGamesByActiveUsers() {
        return gameRepository.findByStates_User_ActiveTrueAndStates_IsBannedFalse();
    }

    public long countAllGames() {
        return gameRepository.count();
    }

    public long countByUsersActive() {
        return gameRepository.countByStates_User_ActiveTrue();
    }
}
