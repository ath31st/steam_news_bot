package bot.farm.steam_news_bot;


import static bot.farm.steam_news_bot.localization.message.MessageEnum.*;
import static bot.farm.steam_news_bot.localization.message.MessageLocalization.getMessage;

import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.SendMessageService;
import bot.farm.steam_news_bot.service.SteamService;
import bot.farm.steam_news_bot.service.UserGameStateService;
import bot.farm.steam_news_bot.service.UserService;
import bot.farm.steam_news_bot.util.UserState;
import java.io.IOException;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Telegram bot for sending Steam news and managing user interactions.
 */
@Component
@RequiredArgsConstructor
public class SteamNewsBot extends TelegramLongPollingBot {
  private final SendMessageService sendMessageService;
  private final UserService userService;
  private final GameService gameService;
  private final UserGameStateService userGameStateService;
  private final SteamService steamService;

  private static final Logger logger = LoggerFactory.getLogger(SteamNewsBot.class);
  @Value("${steamnewsbot.botName}")
  private String botName;
  @Getter
  @Value("${steamnewsbot.botToken}")
  private String botToken;
  private UserState state = UserState.DEFAULT;

  @Override
  public String getBotUsername() {
    return botName;
  }

  /**
   * Processes the received updates from Telegram.
   *
   * @param update the received update.
   */
  @Override
  public void onUpdateReceived(Update update) {

    if (update.hasMessage() && update.getMessage().hasText()) {
      messageProcessing(update.getMessage());
    }

    if (update.hasCallbackQuery()) {
      callBackQueryProcessing(update.getCallbackQuery());
    }
  }

  /**
   * Sends a text message to the specified chat ID.
   *
   * @param chatId the ID of the chat to send the message to.
   * @param text   the text of the message.
   */
  public void sendTextMessage(String chatId, String text) {
    try {
      execute(sendMessageService.createMessage(chatId, text));
    } catch (TelegramApiException e) {
      if (e.getMessage().endsWith("[403] Forbidden: bot was blocked by the user")) {
        userService.updateActiveForUser(chatId, false);

        logger.info("User with chatId {} has received the \"inactive\" status", chatId);
      } else {
        logger.error(e.getMessage());
      }
    }
  }

  /**
   * Sends a news message to the specified chat ID with the given locale.
   *
   * @param chatId the ID of the chat to send the message to.
   * @param text   the text of the news message to send.
   * @param locale the locale of the user.
   */
  public void sendNewsMessage(String chatId, String text, String locale) {
    try {
      execute(sendMessageService.createNewsMessage(chatId, text, locale));
    } catch (TelegramApiException e) {
      if (e.getMessage().endsWith("[403] Forbidden: bot was blocked by the user")) {
        userService.updateActiveForUser(chatId, false);

        logger.info("User with chatId {} has received the \"inactive\" status", chatId);
      } else {
        logger.error(e.getMessage());
      }
    }
  }

  /**
   * Sends a menu message to the specified chat ID with the given message and locale.
   *
   * @param chatId  the ID of the chat to send the message to.
   * @param message the text of the menu message to send.
   * @param locale  the locale of the user.
   */
  private void sendMenuMessage(String chatId, String message, String locale) {
    try {
      execute(sendMessageService.createMenuMessage(chatId, message, locale));
    } catch (TelegramApiException e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * Sets/updates the user's Steam ID and processes outcomes.
   * Validates input, notifies processing, updates/saves user,
   * and handles errors with appropriate messages.
   *
   * @param chatId    Unique chat/user identifier.
   * @param inputText Entered Steam ID.
   * @param locale    Message localization.
   * @param username  User's username.
   */
  private void setUpdateSteamId(String chatId, String inputText, String locale, String username) {
    if (!SteamService.isValidSteamId(inputText)) {
      sendTextMessage(chatId, getMessage(INCORRECT_STEAM_ID, locale));
      return;
    }

    sendTextMessage(chatId, getMessage(WAITING, locale));

    try {
      if (userService.existsByChatId(chatId)) {
        userService.updateUser(chatId, inputText, locale);
      } else {
        userService.saveUser(chatId, username, inputText, locale);
      }

      sendMessageAfterSetUpdateSteamId(chatId, inputText, locale);
    } catch (NullPointerException e) {
      logger.error("User {} entered id {} - this is hidden account", chatId, inputText);
      sendTextMessage(chatId, String.format(getMessage(ERROR_HIDDEN_ACC, locale), inputText));
    } catch (IOException e) {
      logger.error("User {} entered id {}, account dont exists", chatId, inputText);
      sendTextMessage(chatId, String.format(getMessage(ERROR_DONT_EXISTS_ACC, locale), inputText));
    }
  }

  /**
   * Sends a text message after updating the Steam ID for a user and checks if the user is
   * registered.
   * This method first checks if the user with the specified chat ID is registered.
   * If not registered,
   * the method does nothing. If registered, it sends a text message containing information
   * about the user's registration, including their name, count of owned games, and count of
   * wished games.
   *
   * @param chatId    The unique identifier for the chat or user.
   * @param inputText The input text for the message.
   * @param locale    The locale to use for message localization.
   */
  private void sendMessageAfterSetUpdateSteamId(String chatId, String inputText, String locale) {
    if (userService.findUserByChatId(chatId).isEmpty()) {
      return;
    }
    sendTextMessage(chatId, String.format(getMessage(REGISTRATION, locale),
        inputText, userService.findUserByChatId(chatId).orElseThrow().getName(),
        userService.getCountOwnedGames(chatId), userService.getCountWishedGames(chatId)));
  }

  /**
   * Processes incoming messages and takes actions based on the user's state.
   * This method takes a Message object, extracts relevant info such as chat ID,
   * input text, and user locale, and processes the message based on the user's
   * current state. If the user is in the default state, it handles commands like
   * "/start", "/help", "/settings", and default messages. If the user is in the
   * SET_STEAM_ID state, it calls setUpdateSteamId to handle setting or updating
   * the user's Steam ID.
   *
   * @param message The incoming message to be processed.
   */
  private void messageProcessing(Message message) {
    String chatId = String.valueOf(message.getChatId());
    String inputText = message.getText();
    String locale = message.getFrom().getLanguageCode();

    if (Objects.requireNonNull(state) == UserState.DEFAULT) {
      switch (inputText) {
        case "/start" -> sendTextMessage(chatId, getMessage(START, locale));
        case "/help" -> sendTextMessage(chatId, getMessage(HELP, locale));
        case "/settings" -> sendMenuMessage(chatId, getMessage(SETTINGS, locale), locale);
        default -> sendTextMessage(chatId, getMessage(DEFAULT_MESSAGE, locale));
      }
    } else if (state == UserState.SET_STEAM_ID) {
      setUpdateSteamId(chatId, inputText, locale, message.getFrom().getUserName());

      state = UserState.DEFAULT;
    }
  }

  /**
   * Processes callback queries and performs actions based on the received data.
   * This method extracts chat ID and user locale from the callback query. If the user is
   * not registered and the query is not "/set_steam_id," it sends a notification. Otherwise,
   * it handles various callback data, such as setting/updating Steam ID, checking Steam ID,
   * wishlist, toggling active mode, subscribing/unsubscribing, managing blacklists, or sending
   * links to a game. The method updates the user's state accordingly.
   *
   * @param callbackQuery The callback query to be processed.
   */
  private void callBackQueryProcessing(CallbackQuery callbackQuery) {
    String chatId = String.valueOf(callbackQuery.getMessage().getChatId());
    String locale = callbackQuery.getFrom().getLanguageCode();

    if (!userService.existsByChatId(chatId) && !callbackQuery.getData().equals("/set_steam_id")) {
      sendTextMessage(chatId, getMessage(NOT_REGISTERED, locale));
      return;
    }

    switch (callbackQuery.getData()) {
      case "/set_steam_id" -> {
        sendTextMessage(chatId, getMessage(ENTER_STEAM_ID, locale));
        state = UserState.SET_STEAM_ID;
      }
      case "/check_steam_id" ->
          sendTextMessage(chatId, String.format(getMessage(CHECK_STEAM_ID, locale),
              userService.getUserByChatId(chatId).getSteamId())
              + (userService.getUserByChatId(chatId).isActive()
              ? getMessage(ACTIVE, locale) : getMessage(INACTIVE, locale)));
      case "/check_wishlist" -> checkWishlist(chatId, locale);
      case "/set_active_mode" -> {
        userService.updateActiveForUser(chatId, true);
        sendTextMessage(chatId, getMessage(ACTIVE_MODE, locale));
      }
      case "/set_inactive_mode" -> {
        userService.updateActiveForUser(chatId, false);
        sendTextMessage(chatId, getMessage(INACTIVE_MODE, locale));
      }
      case "/unsubscribe" -> unsubscribe(callbackQuery, chatId, locale);
      case "/subscribe" -> subscribe(callbackQuery, chatId, locale);
      case "/links_to_game" -> linksToGame(callbackQuery, chatId, locale);
      case "/black_list" -> blackList(chatId, locale);
      case "/clear_black_list" -> clearBlackList(chatId, locale);
      default -> sendTextMessage(chatId, getMessage(DEFAULT_MESSAGE, locale));
    }
  }

  /**
   * Subscribes the user to receive notifications for a specific game.
   * This method extracts the game title from the callback query, checks if the user is
   * banned from receiving notifications for the game, and updates the subscription state
   * accordingly. It sends a notification about the subscription status.
   *
   * @param callbackQuery The callback query containing information about the game.
   * @param chatId        The unique identifier for the chat or user.
   * @param locale        The locale to use for message localization.
   */
  private void subscribe(CallbackQuery callbackQuery, String chatId, String locale) {
    String gameTitle = callbackQuery.getMessage().getText();
    gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
    if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
      userGameStateService.updateStateForGameByChatId(chatId, gameTitle, false);
      sendTextMessage(chatId, getMessage(SUBSCRIBE, locale) + gameTitle);
    } else {
      sendTextMessage(chatId, getMessage(ALREADY_SUBSCRIBED, locale) + gameTitle);
    }
  }

  /**
   * Checks the availability of the user's Steam wishlist and sends a notification.
   * This method retrieves the user's Steam ID, checks the availability of their wishlist
   * through the Steam service, and sends a notification to the user based on the response code.
   *
   * @param chatId The unique identifier for the chat or user.
   * @param locale The locale to use for message localization.
   */
  private void checkWishlist(String chatId, String locale) {
    long steamId = userService.getUserByChatId(chatId).getSteamId();
    int responseCode = steamService.checkAvailableWishlistBySteamId(steamId);
    String response;
    switch (responseCode) {
      case 200 -> response = String.format(getMessage(WISHLIST_AVAILABLE, locale));
      case 500 -> response = String.format(getMessage(WISHLIST_NOT_AVAILABLE, locale));
      default ->
          response = String.format(getMessage(PROBLEM_WITH_NETWORK_OR_STEAM_SERVICE, locale));
    }
    sendTextMessage(chatId, response);
  }

  private void unsubscribe(CallbackQuery callbackQuery, String chatId, String locale) {
    String gameTitle = callbackQuery.getMessage().getText();
    gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
    if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
      sendTextMessage(chatId, getMessage(ALREADY_UNSUBSCRIBED, locale) + gameTitle);
    } else {
      userGameStateService.updateStateForGameByChatId(chatId, gameTitle, true);
      sendTextMessage(chatId, getMessage(UNSUBSCRIBE, locale) + gameTitle);
    }
  }

  private void linksToGame(CallbackQuery callbackQuery, String chatId, String locale) {
    String gameAppid = callbackQuery.getMessage().getText();
    gameAppid = gameAppid.substring(gameAppid.indexOf("LINK(") + 5, gameAppid.length() - 1);

    sendTextMessage(
        chatId, String.format(getMessage(LINKS_TO_GAME_MESSAGE, locale), gameAppid, gameAppid));
  }

  private void blackList(String chatId, String locale) {
    if (gameService.getBanListByChatId(chatId).isBlank()) {
      sendTextMessage(chatId, getMessage(EMPTY_BLACK_LIST, locale));
    } else {
      sendTextMessage(
          chatId, getMessage(BLACK_LIST, locale) + gameService.getBanListByChatId(chatId));
    }
  }

  private void clearBlackList(String chatId, String locale) {
    if (gameService.getBanListByChatId(chatId).isBlank()) {
      sendTextMessage(chatId, getMessage(EMPTY_BLACK_LIST, locale));
    } else {
      userGameStateService.getBlackListByChatId(chatId)
          .forEach(gameState ->
              userGameStateService.updateStateForGameById(false, gameState.getId()));
      sendTextMessage(chatId, getMessage(BLACK_LIST_CLEAR, locale));
    }
  }
}
