package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.Game;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SteamService {
    @Value("${steamnewsbot.steamwebapikey}")
    private String steamWebApiKey;
    private String ownedGamesUrl = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + steamWebApiKey + "&steamid=";

    public List<Game> getOwnedGames(Long steamId) {
        RestTemplate restTemplate = new RestTemplate();
        Game[] games = restTemplate.getForObject(ownedGamesUrl + steamId, Game[].class);
        return Arrays.stream(games).toList();
    }

    public static boolean isValidSteamId(String steamId) {
        Pattern pattern = Pattern.compile("765[\\d]{14}");
        Matcher matcher = pattern.matcher(steamId);
        return matcher.matches();
    }

    private HttpURLConnection getConnection(String steamId) throws IOException {
        URL url = new URL(ownedGamesUrl + steamId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setRequestMethod("GET");
        return con;
    }

}
