package bot.farm.steam_news_bot.localization.button;

import static bot.farm.steam_news_bot.localization.button.ButtonEnum.BLACK_LIST;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.CHECK_STEAM_ID;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.CHECK_WISHLIST;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.CLEAR_BLACK_LIST;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.LINKS_TO_GAME;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.SET_ACTIVE_MODE;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.SET_INACTIVE_MODE;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.SET_UPD_STEAM_ID;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.SUBSCRIBE;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.UNSUBSCRIBE;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * Utility class for button localization.
 * Provides methods to retrieve localized button labels.
 */
@UtilityClass
public class ButtonLocalization {

  private static final Map<String, String> buttons = new HashMap<>();

  static {
    buttons.put(SET_UPD_STEAM_ID + "_en", "Set/Update Steam ID");
    buttons.put(SET_UPD_STEAM_ID + "_ru", "Ввести/Обновить Steam ID");

    buttons.put(CHECK_STEAM_ID + "_en", "Check your steam ID");
    buttons.put(CHECK_STEAM_ID + "_ru", "Проверить Steam ID");

    buttons.put(SET_ACTIVE_MODE + "_en", "Set \"active\" mode");
    buttons.put(SET_ACTIVE_MODE + "_ru", "Режим \"активен\"");

    buttons.put(SET_INACTIVE_MODE + "_en", "Set \"inactive\" mode");
    buttons.put(SET_INACTIVE_MODE + "_ru", "Режим \"неактивен\"");

    buttons.put(CLEAR_BLACK_LIST + "_en", "Clear black list");
    buttons.put(CLEAR_BLACK_LIST + "_ru", "Очистить черный список");

    buttons.put(BLACK_LIST + "_en", "Black list");
    buttons.put(BLACK_LIST + "_ru", "Черный список");

    buttons.put(UNSUBSCRIBE + "_en", "Unsubscribe");
    buttons.put(UNSUBSCRIBE + "_ru", "Отписаться");

    buttons.put(SUBSCRIBE + "_en", "Subscribe");
    buttons.put(SUBSCRIBE + "_ru", "Подписаться");

    buttons.put(LINKS_TO_GAME + "_en", "Links to the game");
    buttons.put(LINKS_TO_GAME + "_ru", "Ссылки на игру");

    buttons.put(CHECK_WISHLIST + "_en", "Check available wishlist");
    buttons.put(CHECK_WISHLIST + "_ru", "Проверить доступность списка желаемого");
  }

  /**
   * Retrieves a message from the button collection based on the provided key and locale.
   * This method checks if the provided locale is "ru"; if not, it defaults to "en". It then
   * retrieves the corresponding message from the button collection based on the key and locale.
   *
   * @param key    The enumeration key for the desired message.
   * @param locale The locale to use for message retrieval.
   * @return The retrieved message.
   */
  public static String getMessage(Enum<ButtonEnum> key, String locale) {
    if (!locale.equals("ru")) {
      locale = "en";
    }
    return buttons.get(key + "_" + locale);
  }
}
