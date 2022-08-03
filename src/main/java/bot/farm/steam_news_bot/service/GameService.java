package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.Game;
import bot.farm.steam_news_bot.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getAllGames() {
        List<Game> games = new ArrayList<>();
        gameRepository.findAll()
                .iterator()
                .forEachRemaining(games::add);
        return games;
    }

    public Set<Game> getAllGamesByActiveUsers() {
        return gameRepository.findByUsers_ActiveTrue();
    }

    public void saveGamesInDb(List<Game> games) {
        games.stream()
                .filter(game -> !gameRepository.existsByAppid(game.getAppid()))
                .forEach(gameRepository::save);
    }

    public Game getGame(String appid) {
        return gameRepository.getGameByAppid(appid).orElseThrow(
                () -> new RuntimeException(String.format("Game with appid № %s not found!", appid)));
    }

    public long countAllGames() {
        return gameRepository.count();
    }

    public long countByUsersActive(boolean active) {
        return gameRepository.countByUsers_Active(active);
    }
}
