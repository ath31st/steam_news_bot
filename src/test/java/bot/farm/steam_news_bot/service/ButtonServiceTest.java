package bot.farm.steam_news_bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.junit.jupiter.api.Assertions.*;

class ButtonServiceTest {
    private ButtonService buttonService;
    private String locale;

    @BeforeEach
    void setUp() {
        buttonService = new ButtonService();
        locale = "ru";
    }

    @Test
    void createInlineButton() {
        assertNotEquals(0, buttonService.createInlineButton(locale).size());
    }

    @Test
    void createInlineSubscribeButton() {
        assertNotEquals(0, buttonService.createInlineSubscribeButton(locale).size());
    }

    @Test
    void setInlineKeyMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = buttonService.setInlineKeyMarkup(buttonService.createInlineButton(locale));
        assertNotNull(inlineKeyboardMarkup);
    }
}