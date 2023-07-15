package bot.farm.steam_news_bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

/**
 * Service class for creating and configuring messages to be sent.
 */
@Service
@RequiredArgsConstructor
public class SendMessageService {
  private final ButtonService buttonService;

  /**
   * Creates a simple text message to be sent.
   *
   * @param chatId  The chat ID of the recipient.
   * @param message The text message to be sent.
   * @return The created SendMessage object.
   */
  public SendMessage createMessage(String chatId, String message) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.enableMarkdownV2(true);
    sendMessage.enableHtml(true);
    sendMessage.setChatId(chatId);
    sendMessage.setText(message);
    return sendMessage;
  }

  /**
   * Creates a message with an inline keyboard menu.
   *
   * @param chatId  The chat ID of the recipient.
   * @param message The text message to be sent.
   * @param locale  The locale used for localization.
   * @return The created SendMessage object with the inline keyboard menu.
   */
  public SendMessage createMenuMessage(String chatId, String message, String locale) {
    SendMessage sendMessage = createMessage(chatId, message);
    InlineKeyboardMarkup inlineKeyboardMarkup =
        buttonService.setInlineKeyMarkup(buttonService.createInlineButton(locale));
    sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    return sendMessage;
  }

  /**
   * Creates a message with an inline keyboard for subscribing to news.
   *
   * @param chatId  The chat ID of the recipient.
   * @param message The text message to be sent.
   * @param locale  The locale used for localization.
   * @return The created SendMessage object with the inline keyboard for subscribing to news.
   */
  public SendMessage createNewsMessage(String chatId, String message, String locale) {
    SendMessage sendMessage = createMessage(chatId, message);
    InlineKeyboardMarkup inlineKeyboardMarkup =
        buttonService.setInlineKeyMarkup(buttonService.createInlineSubscribeButton(locale));
    sendMessage.setReplyMarkup(inlineKeyboardMarkup);
    return sendMessage;
  }
}
