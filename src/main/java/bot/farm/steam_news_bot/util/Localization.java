package bot.farm.steam_news_bot.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Localization {
    private static final Map<String, String> messages = new HashMap<>();

    static {
        //start
        messages.put("start_en", """
                Welcome! The news Steam bot is at your service!\s
                He has a lot of news for you about releases, patches, events and much more. Hurry up to get started!\s

                Psss. Don't forget to look in the menu -> settings""");
        messages.put("start_ru", """
                Добро пожаловать! Новостной Steam бот к вашим услугам!\s
                У него есть для вас много новостей о релизах, патчах, событиях и многом другом. Спешите начать!\s

                Пссс. Не забудьте заглянуть в меню -> настройки""");

        //help
        messages.put("help_en", """
                1. The news is updated every half hour
                2. If you don't know where to find the Steam ID, then follow the link https://store.steampowered.com/account/
                Steam ID looks like 765XXXXXXXXXX""");
        messages.put("help_ru", """
                1. Новости обновляются каждые полчаса
                2. Если вы не знаете где найти Steam ID, перейдите по ссылке https://store.steampowered.com/account/
                Steam ID выглядит так 765XXXXXXXXXX""");

        //settings
        messages.put("settings_en", "\u2699 Settings");
        messages.put("settings_ru", "\u2699 Настройки");

        //default_message
        messages.put("default_message_en", "Patience, my friend. There is no fresh news yet");
        messages.put("default_message_ru", "Терпение, мой друг. Свежих новостей еще нет");

        //waiting
        messages.put("waiting_en", "It will take a few seconds");
        messages.put("waiting_ru", "Это займет несколько секунд");

        //registration
        messages.put("registration_en", "Your steam ID: %s\nHi %s!\nNice library! You have %d owned games on your account");
        messages.put("registration_ru", "Твой Steam ID: %s\nПривет %s!\nОтличная библиотека! У тебя %d купленных игр на аккаунте");

        //error_hidden_acc
        messages.put("error_hidden_acc_en", "Steam account with id %s is hidden");
        messages.put("error_hidden_acc_ru", "Steam аккаунт с id %s скрыт");

        //error_dont_exists_acc
        messages.put("error_dont_exists_acc_en", "Steam account with id %s don't exists");
        messages.put("error_dont_exists_acc_ru", "Steam аккаунт с id %s не существует");

        //incorrect_steam_id
        messages.put("incorrect_steam_id_en", "You entered an incorrect Steam ID");
        messages.put("incorrect_steam_id_ru", "Вы ввели некорректный Steam ID");

        //enter_steam_id
        messages.put("enter_steam_id_en", "Enter your Steam ID");
        messages.put("enter_steam_id_ru", "Введите ваш Steam ID");

        //check_steam_id
        messages.put("check_steam_id_en", "Your Steam ID: %d\nStatus: ");
        messages.put("check_steam_id_ru", "Ваш Steam ID: %d\nСтатус: ");

        //active
        messages.put("active_en", "active");
        messages.put("active_ru", "активен");

        //inactive
        messages.put("inactive_en", "inactive");
        messages.put("inactive_ru", "неактивен");

        //not_registered
        messages.put("not_registered_en", "You are not registered yet. Please select Set/Update steam ID");
        messages.put("not_registered_ru", "Вы еще не зарегистрированы. Пожалуйста, выберите Установить/Обновить Steam ID");

        //active_mode
        messages.put("active_mode_en", "You are set \"active\" mode. Now the bot will send you news");
        messages.put("active_mode_ru", "Вы поставили режим \"активен\". Теперь бот будет присылать вам новости");

        //inactive_mode
        messages.put("inactive_mode_en", "You are set \"inactive\" mode. Now the bot will not send you news until you activate the \"active\" mode again");
        messages.put("inactive_mode_ru", "Вы поставили режим \"неактивен\". Теперь бот не будет присылать вам новости, пока вы не поставите режим \"активен\" снова");

        //already_unsubscribed
        messages.put("already unsubscribed_en", "You have already unsubscribed from ");
        messages.put("already unsubscribed_ru", "Вы уже отписаны от ");

        //unsubscribe
        messages.put("unsubscribe_en", "You will no longer receive news about ");
        messages.put("unsubscribe_ru", "Вы больше не будете получать новости о ");

        //already_subscribe
        messages.put("already_subscribe_en", "You have already subscribed to this ");
        messages.put("already_subscribe_ru", "Вы уже подписаны на ");

        //subscribe
        messages.put("subscribe_en", "Now you will again receive news about ");
        messages.put("subscribe_ru", "Теперь вы будете получать новости о ");

        //empty_black_list
        messages.put("empty_black_list_en", "Your black list is empty");
        messages.put("empty_black_list_ru", "Ваш черный список пуст");

        //black_list
        messages.put("black_list_en", "Your personal black list: ");
        messages.put("black_list_ru", "Ваш черный список: ");

        //black_list_clear
        messages.put("black_list_clear_en", "Your black list is cleared");
        messages.put("black_list_clear_ru", "Ваш черный список очищен");

    }

    public static String getMessage(String key, String locale) {
        if (!locale.equals("ru")) {
            locale = "en";
        }
        return messages.get(key + "_" + locale);
    }

}
