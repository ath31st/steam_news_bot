package bot.farm.steam_news_bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class ButtonService {

    public List<List<InlineKeyboardButton>> createInlineButton() {
        List<List<InlineKeyboardButton>> inlineKeyButtonList = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonsRow2 = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("\u03BB " + "Set/Update steam ID");
        inlineKeyboardButton1.setCallbackData("/set_steam_id");

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("\u03A9 " + "Check your steam ID");
        inlineKeyboardButton2.setCallbackData("/check_steam_id");

        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText("\u2705 " + "Set \"active\" mode");
        inlineKeyboardButton3.setCallbackData("/set_active_mode");

        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton4.setText("\u2611 " + "Set \"inactive\" mode");
        inlineKeyboardButton4.setCallbackData("/set_inactive_mode");


        inlineKeyboardButtonsRow1.add(inlineKeyboardButton1);
        inlineKeyboardButtonsRow1.add(inlineKeyboardButton2);

        inlineKeyboardButtonsRow2.add(inlineKeyboardButton3);
        inlineKeyboardButtonsRow2.add(inlineKeyboardButton4);

        inlineKeyButtonList.add(inlineKeyboardButtonsRow1);
        inlineKeyButtonList.add(inlineKeyboardButtonsRow2);
        return inlineKeyButtonList;
    }
    public InlineKeyboardMarkup setInlineKeyMarkup(List<List<InlineKeyboardButton>> inlineList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(inlineList);
        return inlineKeyboardMarkup;
    }

}
