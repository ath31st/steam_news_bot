package bot.farm.steam_news_bot.localization.message;

import static bot.farm.steam_news_bot.localization.message.MessageEnum.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MessageLocalization {
  
  private static final Map<String, String> messages = new HashMap<>();
  
  static {
    //START
    messages.put(START + "_en", """
        Welcome! The news Steam bot is at your service!\s
        It tracks news about releases, patches, events and much more that developers have shared on the Steam platform. Hurry up to get started!\s
        
        Psss. Don't forget to look in the menu -> settings""");
    messages.put(START + "_ru", """
        Добро пожаловать! Новостной Steam бот к вашим услугам!\s
        Он отслеживает новости о релизах, патчах, событиях и многом другом, чем поделились разработчики на площадке Steam. Спешите начать!\s
        
        Пссс. Не забудьте заглянуть в меню -> настройки""");
    
    //HELP
    messages.put(HELP + "_en", """
        1. News about your games is updated every half hour
        2. The status of your library is updated once a day
        3. If you don't know where to find the Steam ID, then follow the link https://store.steampowered.com/account/
        Steam ID looks like 765XXXXXXXXXX""");
    messages.put(HELP + "_ru", """
        1. Новости о ваших играх обновляются каждые полчаса
        2. Состояние вашей библиотеки обновляется раз в сутки
        3. Если вы не знаете где найти Steam ID, перейдите по ссылке https://store.steampowered.com/account/
        Steam ID выглядит так: 765XXXXXXXXXX""");
    
    //SETTINGS
    messages.put(SETTINGS + "_en", "\u2699 Settings");
    messages.put(SETTINGS + "_ru", "\u2699 Настройки");
    
    //DEFAULT_MESSAGE
    messages.put(DEFAULT_MESSAGE + "_en", "Patience, my friend. There is no fresh news yet");
    messages.put(DEFAULT_MESSAGE + "_ru", "Терпение, мой друг. Свежих новостей еще нет");
    
    //DEFAULT_NAME
    messages.put(DEFAULT_NAME + "_en", "User");
    messages.put(DEFAULT_NAME + "_ru", "Пользователь");
    
    //WAITING
    messages.put(WAITING + "_en", "It will take a few seconds");
    messages.put(WAITING + "_ru", "Это займет несколько секунд");
    
    //REGISTRATION
    messages.put(REGISTRATION + "_en", "Your steam ID: %s\nHi %s!\nNice library! You have %d owned games on your account");
    messages.put(REGISTRATION + "_ru", "Ваш Steam ID: %s\nПривет, %s!\nОтличная библиотека! У вас %d купленных игр на аккаунте");
    
    //ERROR_HIDDEN_ACC
    messages.put(ERROR_HIDDEN_ACC + "_en", "Steam account with id %s is hidden");
    messages.put(ERROR_HIDDEN_ACC + "_ru", "Steam аккаунт с id %s скрыт");
    
    //ERROR_DONT_EXISTS_ACC
    messages.put(ERROR_DONT_EXISTS_ACC + "_en", "Steam account with id %s don't exists");
    messages.put(ERROR_DONT_EXISTS_ACC + "_ru", "Steam аккаунт с id %s не существует");
    
    //INCORRECT_STEAM_ID
    messages.put(INCORRECT_STEAM_ID + "_en", "You entered an incorrect Steam ID");
    messages.put(INCORRECT_STEAM_ID + "_ru", "Вы ввели некорректный Steam ID");
    
    //ENTER_STEAM_ID
    messages.put(ENTER_STEAM_ID + "_en", "Enter your Steam ID:");
    messages.put(ENTER_STEAM_ID + "_ru", "Введите ваш Steam ID:");
    
    //CHECK_STEAM_ID
    messages.put(CHECK_STEAM_ID + "_en", "Your Steam ID: %d\nStatus: ");
    messages.put(CHECK_STEAM_ID + "_ru", "Ваш Steam ID: %d\nСтатус: ");
    
    //ACTIVE
    messages.put(ACTIVE + "_en", "active");
    messages.put(ACTIVE + "_ru", "активен");
    
    //INACTIVE
    messages.put(INACTIVE + "_en", "inactive");
    messages.put(INACTIVE + "_ru", "неактивен");
    
    //NOT_REGISTERED
    messages.put(NOT_REGISTERED + "_en", "You are not registered yet. Please select Set/Update steam ID");
    messages.put(NOT_REGISTERED + "_ru", "Вы еще не зарегистрированы. Пожалуйста, выберите Ввести/Обновить Steam ID");
    
    //ACTIVE_MODE
    messages.put(ACTIVE_MODE + "_en", "You are set \"active\" mode. Now the bot will send you news");
    messages.put(ACTIVE_MODE + "_ru", "Вы установили режим \"активен\". Теперь бот будет присылать вам новости");
    
    //INACTIVE_MODE
    messages.put(INACTIVE_MODE + "_en", "You are set \"inactive\" mode. Now the bot will not send you news until you activate the \"active\" mode again");
    messages.put(INACTIVE_MODE + "_ru", "Вы установили режим \"неактивен\". Теперь бот не будет присылать вам новости, пока вы не поставите режим \"активен\" снова");
    
    //ALREADY_UNSUBSCRIBED
    messages.put(ALREADY_UNSUBSCRIBED + "_en", "You have already unsubscribed from ");
    messages.put(ALREADY_UNSUBSCRIBED + "_ru", "Вы уже отписаны от ");
    
    //UNSUBSCRIBE
    messages.put(UNSUBSCRIBE + "_en", "You will no longer receive news about ");
    messages.put(UNSUBSCRIBE + "_ru", "Вы больше не будете получать новости о ");
    
    //ALREADY_SUBSCRIBED
    messages.put(ALREADY_SUBSCRIBED + "_en", "You have already subscribed to this ");
    messages.put(ALREADY_SUBSCRIBED + "_ru", "Вы уже подписаны на ");
    
    //SUBSCRIBE
    messages.put(SUBSCRIBE + "_en", "Now you will again receive news about ");
    messages.put(SUBSCRIBE + "_ru", "Теперь вы будете получать новости о ");
    
    //EMPTY_BLACK_LIST
    messages.put(EMPTY_BLACK_LIST + "_en", "Your black list is empty");
    messages.put(EMPTY_BLACK_LIST + "_ru", "Ваш черный список пуст");
    
    //BLACK_LIST
    messages.put(BLACK_LIST + "_en", "Your personal black list: ");
    messages.put(BLACK_LIST + "_ru", "Ваш черный список: ");
    
    //BLACK_LIST_CLEAR
    messages.put(BLACK_LIST_CLEAR + "_en", "Your black list is cleared");
    messages.put(BLACK_LIST_CLEAR + "_ru", "Ваш черный список очищен");
    
    //LINKS_TO_GAME_MESSAGE
    messages.put(LINKS_TO_GAME_MESSAGE + "_en", "<b>Steam store:</b> <a href=\"https://store.steampowered.com/app/%s\">LINK</a>" + "\n"
        + "<b>SteamDB:</b> <a href=\"https://steamdb.info/app/%s\">LINK</a>");
    messages.put(LINKS_TO_GAME_MESSAGE + "_ru", "<b>Steam (официальный магазин):</b> <a href=\"https://store.steampowered.com/app/%s\">ССЫЛКА</a>" + "\n"
        + "<b>SteamDB:</b> <a href=\"https://steamdb.info/app/%s\">ССЫЛКА</a>");
  }
  
  public static String getMessage(Enum<MessageEnum> key, String locale) {
    if (!locale.equals("ru")) {
      locale = "en";
    }
    return messages.get(key + "_" + locale);
  }
  
}
