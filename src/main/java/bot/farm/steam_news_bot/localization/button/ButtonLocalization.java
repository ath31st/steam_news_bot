package bot.farm.steam_news_bot.localization.button;

import static bot.farm.steam_news_bot.localization.button.ButtonEnum.BLACK_LIST;
import static bot.farm.steam_news_bot.localization.button.ButtonEnum.CHECK_STEAM_ID;
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
  }
  
  public static String getMessage(Enum<ButtonEnum> key, String locale) {
    if (!locale.equals("ru")) {
      locale = "en";
    }
    return buttons.get(key + "_" + locale);
  }
}
