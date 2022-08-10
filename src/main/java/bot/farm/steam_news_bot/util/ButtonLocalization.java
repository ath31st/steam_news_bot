package bot.farm.steam_news_bot.util;

import java.util.HashMap;
import java.util.Map;

public class ButtonLocalization {
    public enum Buttons {
        SET_UPD_STEAM_ID,
        CHECK_STEAM_ID,
        SET_ACTIVE_MODE,
        SET_INACTIVE_MODE,
        CLEAR_BLACK_LIST,
        BLACK_LIST,
        UNSUBSCRIBE,
        SUBSCRIBE
    }

    private static final Map<String, String> buttons = new HashMap<>();
    static {
        buttons.put("set_upd_steam_id_en", "Set/Update Steam ID");
        buttons.put("set_upd_steam_id_ru", "Ввести/Обновить Steam ID");
        buttons.put("check_steam_id_en", "Check your steam ID");
        buttons.put("check_steam_id_ru", "Проверить Steam ID");
        buttons.put("set_active_mode_en", "Set \"active\" mode");
        buttons.put("set_active_mode_ru", "Режим \"активен\"");
        buttons.put("set_inactive_mode_en", "Set \"inactive\" mode");
        buttons.put("set_inactive_mode_ru", "Режим \"неактивен\"");
        buttons.put("clear_black_list_en", "Clear black list");
        buttons.put("clear_black_list_ru", "Очистить черный список");
        buttons.put("black_list_en", "Black list");
        buttons.put("black_list_ru", "Черный список");
        buttons.put("unsubscribe_en", "Unsubscribe");
        buttons.put("unsubscribe_ru", "Отписаться");
        buttons.put("subscribe_en", "Subscribe");
        buttons.put("subscribe_ru", "Подписаться");
    }

    public static String getMessage(Enum<Buttons> key, String locale) {
        if (!locale.equals("ru")) {
            locale = "en";
        }
        return buttons.get(key.name().toLowerCase() + "_" + locale);
    }
}
