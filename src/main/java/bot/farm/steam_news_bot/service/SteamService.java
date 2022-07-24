package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.entity.Game;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SteamService {
    @Value("${steamnewsbot.steamwebapikey}")
    private String steamWebApiKey;
    private final String GET_OWNED_GAMES_URL = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=%s&include_appinfo=true&steamid=%s";
    private final String GET_NEWS_FOR_APP_URL = "http://api.steampowered.com/ISteamNews/GetNewsForApp/v2/?appid=%s&count=3&maxlength=300";
    private final static String USER_AGENT = "Mozilla/5.0";

    public List<Game> getOwnedGames(Long steamId) {
        String ownedGamesUrl = String.format(GET_OWNED_GAMES_URL, steamWebApiKey, steamId);

        List<Game> games;
        try {
            URL url = new URL(ownedGamesUrl);
            HttpURLConnection connection = getConnection(url);
            String rawJson = getRawJsonFromConnection(connection);
            games = convertRawJsonToListGames(rawJson);
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return games;
    }

    public List<String> getNewsByOwnedGames(String appid) {
        String newsForAppUrl = String.format(GET_NEWS_FOR_APP_URL, appid);

        List<String> newsItems;
        try {
            URL url = new URL(newsForAppUrl);
            HttpURLConnection connection = getConnection(url);
            String rawJson = getRawJsonFromConnection(connection);
            newsItems = convertRawJsonToListNewsItems(rawJson);
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return newsItems;
    }

    public static boolean isValidSteamId(String steamId) {
        Pattern pattern = Pattern.compile("765[\\d]{14}");
        Matcher matcher = pattern.matcher(steamId);
        return matcher.matches();
    }

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == 404) {
            throw new IllegalArgumentException();
        }
        return connection;
    }

    private String getRawJsonFromConnection(HttpURLConnection connection) {
        StringBuilder response = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response.toString();
    }

    private static List<Game> convertRawJsonToListGames(String rawJson) throws Exception {
        List<Game> ownedGames = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode arrNode = new ObjectMapper()
                .readTree(rawJson)
                .path("response")
                .get("games");
        if (arrNode.isArray()) {
            for (JsonNode objNode : arrNode) {
                ownedGames.add(mapper.convertValue(objNode, Game.class));
            }
        }
        return ownedGames;
    }

    private static List<String> convertRawJsonToListNewsItems(String rawJson) throws Exception {
        List<String> newsItems = new ArrayList<>();

        JsonNode arrNode = new ObjectMapper()
                .readTree(rawJson)
                .path("appnews")
                .get("newsitems");
        if (arrNode.isArray()) {
            for (JsonNode objNode : arrNode) {
                if (checkDateOfNews(Integer.parseInt(objNode.get("date").toString()))) {
                    if (objNode.get("url").toString().isBlank()) {
                        newsItems.add(objNode.get("contents").toString());
                    } else {
                        newsItems.add(objNode.get("url").toString().replaceAll(" ", ""));
                    }
                }
            }
        }
        return newsItems;
    }

    private static boolean checkDateOfNews(int seconds) {
        // LocalDateTime localDateTime = LocalDateTime.from(LocalDateTime.now().atZone(ZoneId.systemDefault()));
        LocalDateTime localDateTime = LocalDateTime.from(LocalDateTime.of(2022, Month.JULY, 23, 15, 00).atZone(ZoneId.systemDefault()));
        LocalDateTime localDateTimeOfNews = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
        return localDateTimeOfNews.toLocalDate().equals(localDateTime.toLocalDate());
    }

}
