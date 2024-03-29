package bot.farm.snb.service;

import bot.farm.snb.entity.Game;
import bot.farm.snb.entity.NewsItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class for interacting with the Steam API to retrieve information about owned games,
 * news items, and wishlist games.
 */
@Service
public class SteamService {
  /**
   * API key for accessing the Steam Web API.
   */
  @Value("${steamnewsbot.steamwebapikey}")
  private String steamWebApiKey;
  private static final Logger logger = LoggerFactory.getLogger(SteamService.class);
  /**
   * URL pattern for retrieving owned games.
   */
  private static final String GET_OWNED_GAMES_URL = "http://api.steampowered.com/IPlayerService/"
      + "GetOwnedGames/v1/?&skip_unvetted_apps=true&key=%s&include_appinfo=true&steamid=%s";
  /**
   * URL pattern for retrieving news items for a specific app.
   */
  private static final String GET_NEWS_FOR_APP_URL = "http://api.steampowered.com/ISteamNews/"
      + "GetNewsForApp/v2/?appid=%s&count=3&maxlength=300";
  /**
   * URL pattern for retrieving wishlist games.
   */
  private static final String GET_WISHLIST_GAMES_URL = "https://store.steampowered.com/"
      + "wishlist/profiles/%s/wishlistdata/";
  /**
   * User agent string for making HTTP requests.
   */
  private static final String USER_AGENT = "Mozilla/5.0";

  /**
   * Retrieves the owned games for a given Steam user ID.
   *
   * @param steamId the Steam user ID
   * @return the list of owned games
   * @throws IOException          if an I/O error occurs while making the request
   * @throws NullPointerException if the account is hidden or does not exist
   */

  public List<Game> getOwnedGames(Long steamId) throws IOException, NullPointerException {
    String ownedGamesUrl = String.format(GET_OWNED_GAMES_URL, steamWebApiKey, steamId);

    List<Game> games;

    URL url = new URL(ownedGamesUrl);
    HttpURLConnection connection = getConnection(url);
    String rawJson = getRawDataFromConnection(connection);

    if (rawJson.equals("{\"response\":{}}") || rawJson.equals("{\"success\": 2")) {
      throw new NullPointerException("Account is hidden");
    }

    games = convertRawJsonToListGames(rawJson);
    connection.disconnect();

    return games;
  }

  /**
   * Retrieves the news items for a specific game app.
   *
   * @param appid the app ID of the game
   * @return the list of news items
   * @throws IOException if an I/O error occurs while making the request
   */
  public List<NewsItem> getNewsByOwnedGames(String appid) throws IOException {
    String newsForAppUrl = String.format(GET_NEWS_FOR_APP_URL, appid);

    List<NewsItem> newsItems;
    URL url = new URL(newsForAppUrl);
    HttpURLConnection connection = getConnection(url);
    String rawJson = getRawDataFromConnection(connection);
    newsItems = convertRawJsonToListNewsItems(rawJson);
    connection.disconnect();

    return newsItems;
  }

  /**
   * Retrieves the wishlist games for a given Steam user ID.
   *
   * @param steamId the Steam user ID
   * @return the list of wishlist games
   */
  public List<Game> getWishListGames(Long steamId) {
    String wishListGamesUrl = String.format(GET_WISHLIST_GAMES_URL, steamId);

    List<Game> wishListGames = new ArrayList<>();
    try {
      URL url = new URL(wishListGamesUrl);
      HttpURLConnection connection = getConnection(url);
      String rawJson = getRawDataFromConnection(connection);
      wishListGames.addAll(convertRawJsonToWishListGames(rawJson));
      connection.disconnect();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return wishListGames;
  }

  /**
   * Checks if a given Steam ID is valid.
   *
   * @param steamId the Steam ID to validate
   * @return true if the Steam ID is valid, false otherwise
   */
  public static boolean isValidSteamId(String steamId) {
    Pattern pattern = Pattern.compile("765[\\d]{14}");
    Matcher matcher = pattern.matcher(steamId);
    return matcher.matches();
  }

  /**
   * Checks the availability of a user's Steam wishlist by Steam ID.
   * This method constructs the wishlist games URL using the provided Steam ID, sends a
   * request to the Steam API, and retrieves the response code. It handles potential
   * exceptions such as an invalid argument or an IO error.
   *
   * @param steamId The Steam ID of the user.
   * @return The response code indicating the availability of the wishlist.
   */
  public int checkAvailableWishlistBySteamId(Long steamId) {
    String wishListGamesUrl = String.format(GET_WISHLIST_GAMES_URL, steamId);
    int responseCode;
    try {
      URL url = new URL(wishListGamesUrl);
      HttpURLConnection connection = getConnection(url);
      responseCode = connection.getResponseCode();
      connection.disconnect();
    } catch (IllegalArgumentException e) {
      responseCode = 500;
    } catch (IOException e) {
      responseCode = 100;
    }
    return responseCode;
  }

  /**
   * Establishes and configures an HTTP connection for the given URL.
   * This method creates an HttpURLConnection for the provided URL, sets headers,
   * timeouts, and request method. It also checks the initial response code and throws
   * an IllegalArgumentException if it's 404 or 500.
   *
   * @param url The URL for which the connection is established.
   * @return The configured HttpURLConnection.
   * @throws IOException If an I/O error occurs while opening the connection.
   */
  private HttpURLConnection getConnection(URL url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("User-Agent", USER_AGENT);
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);
    connection.setRequestMethod("GET");
    int responseCode = connection.getResponseCode();
    if (responseCode == 404 || responseCode == 500) {
      connection.disconnect();
      throw new IllegalArgumentException();
    }
    return connection;
  }

  /**
   * Reads and retrieves raw data from the provided HTTP connection.
   * This method reads the input stream from the connection and constructs a string
   * containing the raw data. It then closes the BufferedReader and returns the raw data.
   *
   * @param connection The HttpURLConnection from which to read raw data.
   * @return The raw data retrieved from the connection.
   * @throws IOException If an I/O error occurs while reading the input stream.
   */
  private String getRawDataFromConnection(HttpURLConnection connection) throws IOException {
    StringBuilder response = new StringBuilder();
    BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String inputLine;
    while ((inputLine = bufferedReader.readLine()) != null) {
      response.append(inputLine);
    }
    bufferedReader.close();
    return response.toString();
  }

  /**
   * Converts raw JSON data to a list of Game objects.
   * This method uses the Jackson ObjectMapper to convert the raw JSON data into a list
   * of Game objects. It specifically extracts the "games" array from the "response" field
   * in the JSON structure.
   *
   * @param rawJson The raw JSON data to be converted.
   * @return A list of Game objects parsed from the JSON data.
   * @throws JsonProcessingException If an error occurs during JSON processing.
   */
  private static List<Game> convertRawJsonToListGames(String rawJson)
      throws JsonProcessingException {
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

  /**
   * Converts raw JSON data to a list of Game objects representing a wishlist.
   * This method uses the Jackson ObjectMapper to convert the raw JSON data into a list
   * of Game objects. It specifically processes an object where each field represents a
   * game in the wishlist, extracting appid and name information.
   *
   * @param rawJson The raw JSON data to be converted.
   * @return A list of Game objects representing games in the wishlist.
   * @throws JsonProcessingException If an error occurs during JSON processing.
   */
  private static List<Game> convertRawJsonToWishListGames(String rawJson)
      throws JsonProcessingException {
    List<Game> wishListGames = new ArrayList<>();
    JsonNode jsonNode = new ObjectMapper().readTree(rawJson);

    if (jsonNode.isObject()) {
      jsonNode.fields().forEachRemaining(node -> {
        Game game = new Game();
        game.setAppid(node.getKey());
        game.setName(node.getValue().get("name").toString());
        wishListGames.add(game);
      });
    }

    return wishListGames;
  }

  /**
   * Converts raw JSON data to a list of NewsItem objects.
   * This method uses the Jackson ObjectMapper to convert the raw JSON data into a list
   * of NewsItem objects. It specifically extracts the "newsitems" array from the "appnews"
   * field in the JSON structure and filters items based on the date.
   *
   * @param rawJson The raw JSON data to be converted.
   * @return A list of filtered NewsItem objects parsed from the JSON data.
   * @throws JsonProcessingException If an error occurs during JSON processing.
   */
  private static List<NewsItem> convertRawJsonToListNewsItems(String rawJson)
      throws JsonProcessingException {
    List<NewsItem> newsItems = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();

    JsonNode arrNode = new ObjectMapper()
        .readTree(rawJson)
        .path("appnews")
        .get("newsitems");
    if (arrNode.isArray()) {
      for (JsonNode objNode : arrNode) {
        if (checkDateOfNews(Integer.parseInt(objNode.get("date").toString()))) {
          NewsItem newsItem = mapper.convertValue(objNode, NewsItem.class);
          newsItem.setContents(deleteLinksOnImagesFromText(newsItem.getContents()));
          newsItems.add(newsItem);
        }
      }
    }
    return newsItems;
  }

  /**
   * Checks if the news date is within the last 30 minutes.
   * This method compares the date of the news, given in seconds since the epoch, with
   * the current date and time. It returns true if the news is within the last 30 minutes.
   *
   * @param seconds The news date in seconds since the epoch.
   * @return True if the news is within the last 30 minutes, false otherwise.
   */
  private static boolean checkDateOfNews(int seconds) {
    LocalDateTime localDateTime =
        LocalDateTime.from(LocalDateTime.now().atZone(ZoneId.systemDefault()));
    LocalDateTime localDateTimeOfNews =
        LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());

    // TODO CHECK THIS LINE!
    return localDateTimeOfNews.plus(1800000, ChronoUnit.MILLIS).isAfter(localDateTime);
    //return localDateTimeOfNews.toLocalDate().isEqual(localDateTime.toLocalDate());
  }

  /**
   * Deletes links to images from the given text.
   * This method uses a regular expression to find and remove links to images
   * (e.g., {STEAM_IMAGE_URL.jpg}) from the provided text.
   *
   * @param text The text from which to delete image links.
   * @return The text with image links removed.
   */
  private static String deleteLinksOnImagesFromText(String text) {
    Pattern pattern = Pattern.compile("\\{STEAM.*((.jpg)|(.png)|(.gif))\\b|\\{STEAM.*");
    Matcher matcher = pattern.matcher(text);
    return matcher.replaceAll("");
  }
}


