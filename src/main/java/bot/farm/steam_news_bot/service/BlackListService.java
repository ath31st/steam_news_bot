package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.BlackListGame;
import bot.farm.steam_news_bot.repository.BlackListGameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlackListService {
    private final BlackListGameRepository blackListGameRepository;
    private final GameService gameService;

    public BlackListService(BlackListGameRepository blackListGameRepository, GameService gameService) {
        this.blackListGameRepository = blackListGameRepository;
        this.gameService = gameService;
    }

    public boolean existsByChatIdAndName(String chatId, String name) {
        return blackListGameRepository.existsByChatIdAndName(chatId, name);
    }

    public void addGameToBlackList(String chatId, String name) {
        if (blackListGameRepository.findByChatIdAndName(chatId, name).isEmpty()) {
            BlackListGame game = new BlackListGame();
            game.setAppid(gameService.getGame(name).getAppid());
            game.setName(name);
            game.setChatId(chatId);
            blackListGameRepository.save(game);
        }
    }

    public void removeGameFromBlackList(String chatId, String name) {
        if (blackListGameRepository.findByChatIdAndName(chatId, name).isPresent()) {
            blackListGameRepository.deleteByChatIdAndName(chatId,name);
        }
    }

    public List<BlackListGame> getBlackListByChatId(String chatId) {
        return blackListGameRepository.findByChatId(chatId);
    }

    public String getBlackListForPrint(String chatId) {
        List<BlackListGame> blackListGames = blackListGameRepository.findByChatId(chatId);
        return blackListGames.stream()
                .map(BlackListGame::getName)
                .collect(Collectors.joining(", "));
    }
}
