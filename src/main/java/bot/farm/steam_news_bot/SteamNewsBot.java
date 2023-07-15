package bot.farm.steam_news_bot;


import static bot.farm.steam_news_bot.localization.message.MessageEnum.ACTIVE;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.ACTIVE_MODE;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.ALREADY_UNSUBSCRIBED;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.BLACK_LIST;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.BLACK_LIST_CLEAR;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.CHECK_STEAM_ID;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.DEFAULT_MESSAGE;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.EMPTY_BLACK_LIST;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.ENTER_STEAM_ID;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.ERROR_DONT_EXISTS_ACC;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.ERROR_HIDDEN_ACC;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.HELP;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.INACTIVE;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.INACTIVE_MODE;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.INCORRECT_STEAM_ID;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.LINKS_TO_GAME_MESSAGE;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.NOT_REGISTERED;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.REGISTRATION;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.SETTINGS;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.START;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.UNSUBSCRIBE;
import static bot.farm.steam_news_bot.localization.message.MessageEnum.WAITING;
import static bot.farm.steam_news_bot.localization.message.MessageLocalization.getMessage;

import bot.farm.steam_news_bot.service.GameService;
import bot.farm.steam_news_bot.service.SendMessageService;
import bot.farm.steam_news_bot.service.SteamService;
import bot.farm.steam_news_bot.service.UserGameStateService;
import bot.farm.steam_news_bot.service.UserService;
import bot.farm.steam_news_bot.util.UserState;
import java.io.IOException;
import java.util.Objects;
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
public class SteamNewsBot extends TelegramLongPollingBot {
  private final SendMessageService sendMessageService;
  private final UserService userService;
  private final GameService gameService;
  private final UserGameStateService userGameStateService;

  private static final Logger logger = LoggerFactory.getLogger(SteamNewsBot.class);
  @Value("${steamnewsbot.botName}")
  private String botName;
  @Value("${steamnewsbot.botToken}")
  private String botToken;
  private UserState state = UserState.DEFAULT;

  /**
   * Constructor for the SteamNewsBot class.
   *
   * @param sendMessageService   the service for sending messages.
   * @param userService          the service for managing user data.
   * @param gameService          the service for managing game data.
   * @param userGameStateService the service for managing user game states.
   */
  public SteamNewsBot(SendMessageService sendMessageService,
                      UserService userService,
                      GameService gameService,
                      UserGameStateService userGameStateService) {
    this.sendMessageService = sendMessageService;
    this.userService = userService;
    this.gameService = gameService;
    this.userGameStateService = userGameStateService;
  }

  @Override
  public String getBotUsername() {
    return botName;
  }

  @Override
  public String getBotToken() {
    return botToken;
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

  private void sendMessageAfterSetUpdateSteamId(String chatId, String inputText, String locale) {
    if (userService.findUserByChatId(chatId).isEmpty()) {
      return;
    }
    sendTextMessage(chatId, String.format(getMessage(REGISTRATION, locale),
        inputText, userService.findUserByChatId(chatId).orElseThrow().getName(),
        userService.getCountOwnedGames(chatId)));
  }

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

      case "/set_active_mode" -> {
        userService.updateActiveForUser(chatId, true);
        sendTextMessage(chatId, getMessage(ACTIVE_MODE, locale));
      }
      case "/set_inactive_mode" -> {
        userService.updateActiveForUser(chatId, false);
        sendTextMessage(chatId, getMessage(INACTIVE_MODE, locale));
      }
      case "/unsubscribe" -> {
        String gameTitle = callbackQuery.getMessage().getText();
        gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
        if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
          sendTextMessage(chatId, getMessage(ALREADY_UNSUBSCRIBED, locale) + gameTitle);
        } else {
          userGameStateService.updateStateForGameByChatId(chatId, gameTitle, true);
          sendTextMessage(chatId, getMessage(UNSUBSCRIBE, locale) + gameTitle);
        }
      }
//      case "/subscribe" -> {
//        String gameTitle = callbackQuery.getMessage().getText();
//        gameTitle = gameTitle.substring(0, gameTitle.indexOf("\n"));
//        if (userService.checkBanForGameByChatId(chatId, gameTitle)) {
//          userGameStateService.updateStateForGameByChatId(chatId, gameTitle, false);
//          sendTextMessage(chatId, getMessage(SUBSCRIBE, locale) + gameTitle);
//        } else {
//          sendTextMessage(chatId, getMessage(ALREADY_SUBSCRIBED, locale) + gameTitle);
//        }
//      }
      case "/links_to_game" -> {
        String gameAppid = callbackQuery.getMessage().getText();
        gameAppid = gameAppid.substring(gameAppid.indexOf("LINK(") + 5, gameAppid.length() - 1);

        sendTextMessage(
            chatId, String.format(getMessage(LINKS_TO_GAME_MESSAGE, locale), gameAppid, gameAppid));
      }

      case "/black_list" -> {
        if (gameService.getBanListByChatId(chatId).isBlank()) {
          sendTextMessage(chatId, getMessage(EMPTY_BLACK_LIST, locale));
        } else {
          sendTextMessage(
              chatId, getMessage(BLACK_LIST, locale) + gameService.getBanListByChatId(chatId));
        }
      }
      case "/clear_black_list" -> {
        if (gameService.getBanListByChatId(chatId).isBlank()) {
          sendTextMessage(chatId, getMessage(EMPTY_BLACK_LIST, locale));
        } else {
          userGameStateService.getBlackListByChatId(chatId)
              .forEach(gameState ->
                  userGameStateService.updateStateForGameById(false, gameState.getId()));
          sendTextMessage(chatId, getMessage(BLACK_LIST_CLEAR, locale));
        }
      }
      default -> sendTextMessage(chatId, getMessage(DEFAULT_MESSAGE, locale));

    }
  }
}
