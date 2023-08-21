package bot.farm.steam_news_bot.service;

import static bot.farm.steam_news_bot.localization.button.ButtonEnum.*;
import static bot.farm.steam_news_bot.localization.button.ButtonLocalization.getMessage;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

/**
 * Service class for creating inline buttons and inline keyboard markup.
 */
@Service
public class ButtonService {
  /**
   * Creates a list of inline buttons for the main keyboard.
   *
   * @param locale The locale used for getting localized messages.
   * @return The list of inline buttons.
   */
  public List<List<InlineKeyboardButton>> createInlineButton(String locale) {
    final List<List<InlineKeyboardButton>> inlineKeyButtonList = new ArrayList<>();
    final List<InlineKeyboardButton> inlineKeyboardButtonsRow1 = new ArrayList<>();
    final List<InlineKeyboardButton> inlineKeyboardButtonsRow2 = new ArrayList<>();
    final List<InlineKeyboardButton> inlineKeyboardButtonsRow3 = new ArrayList<>();
    final List<InlineKeyboardButton> inlineKeyboardButtonsRow4 = new ArrayList<>();

    InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
    inlineKeyboardButton1.setText("λ " + getMessage(SET_UPD_STEAM_ID, locale));
    inlineKeyboardButton1.setCallbackData("/set_steam_id");

    InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
    inlineKeyboardButton2.setText("Ω " + getMessage(CHECK_STEAM_ID, locale));
    inlineKeyboardButton2.setCallbackData("/check_steam_id");

    InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
    inlineKeyboardButton3.setText("✅ " + getMessage(SET_ACTIVE_MODE, locale));
    inlineKeyboardButton3.setCallbackData("/set_active_mode");

    InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
    inlineKeyboardButton4.setText("☑ " + getMessage(SET_INACTIVE_MODE, locale));
    inlineKeyboardButton4.setCallbackData("/set_inactive_mode");

    InlineKeyboardButton inlineKeyboardButton5 = new InlineKeyboardButton();
    inlineKeyboardButton5.setText("\uD83E\uDDF9 " + getMessage(CLEAR_BLACK_LIST, locale));
    inlineKeyboardButton5.setCallbackData("/clear_black_list");

    InlineKeyboardButton inlineKeyboardButton6 = new InlineKeyboardButton();
    inlineKeyboardButton6.setText("\uD83D\uDDD1 " + getMessage(BLACK_LIST, locale));
    inlineKeyboardButton6.setCallbackData("/black_list");

    InlineKeyboardButton inlineKeyboardButton7 = new InlineKeyboardButton();
    inlineKeyboardButton7.setText("♥ " + getMessage(CHECK_WISHLIST, locale));
    inlineKeyboardButton7.setCallbackData("/check_wishlist");

    inlineKeyboardButtonsRow1.add(inlineKeyboardButton1);
    inlineKeyboardButtonsRow1.add(inlineKeyboardButton2);

    inlineKeyboardButtonsRow2.add(inlineKeyboardButton3);
    inlineKeyboardButtonsRow2.add(inlineKeyboardButton4);

    inlineKeyboardButtonsRow3.add(inlineKeyboardButton5);
    inlineKeyboardButtonsRow3.add(inlineKeyboardButton6);

    inlineKeyboardButtonsRow4.add(inlineKeyboardButton7);

    inlineKeyButtonList.add(inlineKeyboardButtonsRow1);
    inlineKeyButtonList.add(inlineKeyboardButtonsRow4);
    inlineKeyButtonList.add(inlineKeyboardButtonsRow2);
    inlineKeyButtonList.add(inlineKeyboardButtonsRow3);
    return inlineKeyButtonList;
  }

  /**
   * Creates a list of inline buttons for the subscribe keyboard.
   *
   * @param locale The locale used for getting localized messages.
   * @return The list of inline buttons.
   */
  public List<List<InlineKeyboardButton>> createInlineSubscribeButton(String locale) {
    final List<List<InlineKeyboardButton>> inlineKeyButtonList = new ArrayList<>();
    final List<InlineKeyboardButton> inlineKeyboardButtonsRow1 = new ArrayList<>();

    InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
    inlineKeyboardButton1.setText("\uD83D\uDEAB " + getMessage(UNSUBSCRIBE, locale));
    inlineKeyboardButton1.setCallbackData("/unsubscribe");

    InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
    inlineKeyboardButton2.setText("\uD83D\uDD17 " + getMessage(LINKS_TO_GAME, locale));
    inlineKeyboardButton2.setCallbackData("/links_to_game");

    inlineKeyboardButtonsRow1.add(inlineKeyboardButton1);
    inlineKeyboardButtonsRow1.add(inlineKeyboardButton2);

    inlineKeyButtonList.add(inlineKeyboardButtonsRow1);
    return inlineKeyButtonList;
  }

  /**
   * Creates an inline keyboard markup using the provided list of inline buttons.
   *
   * @param inlineList The list of inline buttons.
   * @return The inline keyboard markup.
   */
  public InlineKeyboardMarkup setInlineKeyMarkup(List<List<InlineKeyboardButton>> inlineList) {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    inlineKeyboardMarkup.setKeyboard(inlineList);
    return inlineKeyboardMarkup;
  }

}
