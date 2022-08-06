package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.User;
import bot.farm.steam_news_bot.entity.UserGameState;
import bot.farm.steam_news_bot.repository.UserGameStateRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserGameStateService {
    private final UserGameStateRepository userGameStateRepository;
    private final SteamService steamService;

    public UserGameStateService(UserGameStateRepository userGameStateRepository, SteamService steamService) {
        this.userGameStateRepository = userGameStateRepository;
        this.steamService = steamService;
    }

//    public Set<UserGameState> getSetStates(User user) {
//        return steamService.getOwnedGames(user.getSteamId())
//                .stream()
//                .parallel()
//                .map(game -> {
//                    UserGameState userGameState = new UserGameState();
//                    userGameState.setUser(user);
//                    userGameState.setGame(game);
//                    userGameState.setBanned(false);
//                    userGameState.setWished(false);
//                    return userGameState;
//                })
//                .collect(Collectors.toSet());
//    }

}
