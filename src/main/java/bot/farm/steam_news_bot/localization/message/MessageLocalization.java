package bot.farm.steam_news_bot.localization.message;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * Utility class for localizing messages based on the provided locale.
 */
@UtilityClass
public class MessageLocalization {

  private static final Map<String, String> messages = new HashMap<>();

  static {
    //START
    messages.put(MessageEnum.START + "_en", """
        Welcome! The news Steam bot is at your service!\s
        It tracks news about releases, patches, events and much more that developers have shared\s
        on the Steam platform. Hurry up to get started!\s
                
        Psss. Don't forget to look in the menu -> settings""");
    messages.put(MessageEnum.START + "_ru", """
        Добро пожаловать! Новостной Steam бот к вашим услугам!\s
        Он отслеживает новости о релизах, патчах, событиях и многом другом, чем поделились\s
        разработчики на площадке Steam. Спешите начать!\s
                
        Пссс. Не забудьте заглянуть в меню -> настройки""");

    //HELP
    messages.put(MessageEnum.HELP + "_en", """
        1. News about your games is updated every half hour
        2. The status of your library is updated once a day
        3. If your wishlist has not been added, then check the privacy settings in Steam by following the link https://steamcommunity.com/id/{YOUR_ID}/edit/settings. They should be like this: "My profile: Public" and "Game details: Public"
        4. If you don't know where to find the Steam ID, then follow the link https://store.steampowered.com/account/
        Steam ID looks like 765XXXXXXXXXX""");
    messages.put(MessageEnum.HELP + "_ru", """
        1. Новости о ваших играх обновляются каждые полчаса
        2. Состояние вашей библиотеки обновляется раз в сутки
        3. Если ваш список желаемого не добавился, то проверьте настройки приватности в Steam по ссылке https://steamcommunity.com/id/{YOUR_ID}/edit/settings. Они должны быть такими: "Мой профиль: Открытый" и "Доступ к игровой информации: Открытый"
        4. Если вы не знаете где найти Steam ID, перейдите по ссылке https://store.steampowered.com/account/
        Steam ID выглядит так: 765XXXXXXXXXX""");

    //SETTINGS
    messages.put(MessageEnum.SETTINGS + "_en", "⚙ Settings");
    messages.put(MessageEnum.SETTINGS + "_ru", "⚙ Настройки");

    //DEFAULT_MESSAGE
    messages.put(MessageEnum.DEFAULT_MESSAGE + "_en",
        "Patience, my friend. There is no fresh news yet");
    messages.put(MessageEnum.DEFAULT_MESSAGE + "_ru",
        "Терпение, мой друг. Свежих новостей еще нет");

    //DEFAULT_NAME
    messages.put(MessageEnum.DEFAULT_NAME + "_en", "User");
    messages.put(MessageEnum.DEFAULT_NAME + "_ru", "Пользователь");

    //WAITING
    messages.put(MessageEnum.WAITING + "_en", "It will take a few seconds");
    messages.put(MessageEnum.WAITING + "_ru", "Это займет несколько секунд");

    //REGISTRATION
    messages.put(MessageEnum.REGISTRATION + "_en", """
        Your steam ID: %s
        Hi %s!
        Nice library!
        You have %d owned games on your account and %d in wishlist""");
    messages.put(MessageEnum.REGISTRATION + "_ru", """
        Ваш Steam ID: %s
        Привет, %s!
        Отличная библиотека!
        У вас %d купленных игр на аккаунте и %d в списке желаемого""");

    //ERROR_HIDDEN_ACC
    messages.put(MessageEnum.ERROR_HIDDEN_ACC + "_en", "Steam account with id %s is hidden");
    messages.put(MessageEnum.ERROR_HIDDEN_ACC + "_ru", "Steam аккаунт с id %s скрыт");

    //ERROR_DONT_EXISTS_ACC
    messages.put(MessageEnum.ERROR_DONT_EXISTS_ACC + "_en",
        "Steam account with id %s don't exists");
    messages.put(MessageEnum.ERROR_DONT_EXISTS_ACC + "_ru", "Steam аккаунт с id %s не существует");

    //INCORRECT_STEAM_ID
    messages.put(MessageEnum.INCORRECT_STEAM_ID + "_en", "You entered an incorrect Steam ID");
    messages.put(MessageEnum.INCORRECT_STEAM_ID + "_ru", "Вы ввели некорректный Steam ID");

    //ENTER_STEAM_ID
    messages.put(MessageEnum.ENTER_STEAM_ID + "_en", "Enter your Steam ID:");
    messages.put(MessageEnum.ENTER_STEAM_ID + "_ru", "Введите ваш Steam ID:");

    //CHECK_STEAM_ID
    messages.put(MessageEnum.CHECK_STEAM_ID + "_en", "Your Steam ID: %d\nStatus: ");
    messages.put(MessageEnum.CHECK_STEAM_ID + "_ru", "Ваш Steam ID: %d\nСтатус: ");

    //ACTIVE
    messages.put(MessageEnum.ACTIVE + "_en", "active");
    messages.put(MessageEnum.ACTIVE + "_ru", "активен");

    //INACTIVE
    messages.put(MessageEnum.INACTIVE + "_en", "inactive");
    messages.put(MessageEnum.INACTIVE + "_ru", "неактивен");

    //NOT_REGISTERED
    messages.put(MessageEnum.NOT_REGISTERED + "_en",
        "You are not registered yet. Please select Set/Update steam ID");
    messages.put(MessageEnum.NOT_REGISTERED + "_ru",
        "Вы еще не зарегистрированы. Пожалуйста, выберите Ввести/Обновить Steam ID");

    //ACTIVE_MODE
    messages.put(MessageEnum.ACTIVE_MODE + "_en",
        "You are set \"active\" mode. Now the bot will send you news");
    messages.put(MessageEnum.ACTIVE_MODE + "_ru",
        "Вы установили режим \"активен\". Теперь бот будет присылать вам новости");

    //INACTIVE_MODE
    messages.put(MessageEnum.INACTIVE_MODE + "_en",
        "You are set \"inactive\" mode. Now the bot will not send you news until you "
            + "activate the \"active\" mode again");
    messages.put(MessageEnum.INACTIVE_MODE + "_ru",
        "Вы установили режим \"неактивен\". Теперь бот не будет присылать вам новости, "
            + "пока вы не поставите режим \"активен\" снова");

    //ALREADY_UNSUBSCRIBED
    messages.put(MessageEnum.ALREADY_UNSUBSCRIBED + "_en", "You have already unsubscribed from ");
    messages.put(MessageEnum.ALREADY_UNSUBSCRIBED + "_ru", "Вы уже отписаны от ");

    //UNSUBSCRIBE
    messages.put(MessageEnum.UNSUBSCRIBE + "_en", "You will no longer receive news about ");
    messages.put(MessageEnum.UNSUBSCRIBE + "_ru", "Вы больше не будете получать новости о ");

    //ALREADY_SUBSCRIBED
    messages.put(MessageEnum.ALREADY_SUBSCRIBED + "_en", "You have already subscribed to this ");
    messages.put(MessageEnum.ALREADY_SUBSCRIBED + "_ru", "Вы уже подписаны на ");

    //SUBSCRIBE
    messages.put(MessageEnum.SUBSCRIBE + "_en", "Now you will again receive news about ");
    messages.put(MessageEnum.SUBSCRIBE + "_ru", "Теперь вы будете получать новости о ");

    //EMPTY_BLACK_LIST
    messages.put(MessageEnum.EMPTY_BLACK_LIST + "_en", "Your black list is empty");
    messages.put(MessageEnum.EMPTY_BLACK_LIST + "_ru", "Ваш черный список пуст");

    //BLACK_LIST
    messages.put(MessageEnum.BLACK_LIST + "_en", "Your personal black list: ");
    messages.put(MessageEnum.BLACK_LIST + "_ru", "Ваш черный список: ");

    //BLACK_LIST_CLEAR
    messages.put(MessageEnum.BLACK_LIST_CLEAR + "_en", "Your black list is cleared");
    messages.put(MessageEnum.BLACK_LIST_CLEAR + "_ru", "Ваш черный список очищен");

    //WISHLIST_AVAILABLE
    messages.put(MessageEnum.WISHLIST_AVAILABLE + "_en", "Your wishlist is available");
    messages.put(MessageEnum.WISHLIST_AVAILABLE + "_ru", "Ваш список желаемого доступен");

    //WISHLIST_NOT_AVAILABLE
    messages.put(MessageEnum.WISHLIST_NOT_AVAILABLE + "_en",
        "Your wishlist is unavailable. Refer to /help to solve the problem");
    messages.put(MessageEnum.WISHLIST_NOT_AVAILABLE + "_ru",
        "Ваш список желаемого недоступен. Обратитесь к /help для решения проблемы");

    //PROBLEM_WITH_NETWORK_OR_STEAM_SERVICE
    messages.put(MessageEnum.PROBLEM_WITH_NETWORK_OR_STEAM_SERVICE + "_en",
        "There is a problem with the network or the Steam service. Try again later");
    messages.put(MessageEnum.PROBLEM_WITH_NETWORK_OR_STEAM_SERVICE + "_ru",
        "Возникла проблема с сетью или сервисом Steam. Повторите попытку позже");

    //LINKS_TO_GAME_MESSAGE
    messages.put(MessageEnum.LINKS_TO_GAME_MESSAGE + "_en",
        "<b>Steam store:</b> <a href=\"https://store.steampowered.com/app/%s\">LINK</a>" + "\n"
            + "<b>SteamDB:</b> <a href=\"https://steamdb.info/app/%s\">LINK</a>");
    messages.put(MessageEnum.LINKS_TO_GAME_MESSAGE + "_ru",
        "<b>Steam (официальный магазин):</b> "
            + "<a href=\"https://store.steampowered.com/app/%s\">ССЫЛКА</a>" + "\n"
            + "<b>SteamDB:</b> <a href=\"https://steamdb.info/app/%s\">ССЫЛКА</a>");
  }

  /**
   * Retrieves a message from the resources based on the provided key and locale.
   *
   * @param key    The message key represented as an Enum of type MessageEnum.
   * @param locale The locale indicating the language of the message (e.g., "en" for English).
   * @return A string containing the message from the resources for the specified key and locale.
   */
  public static String getMessage(Enum<MessageEnum> key, String locale) {
    if (!locale.equals("ru")) {
      locale = "en";
    }
    return messages.get(key + "_" + locale);
  }
}
