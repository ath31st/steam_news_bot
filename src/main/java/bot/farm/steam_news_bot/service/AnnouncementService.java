package bot.farm.steam_news_bot.service;

import bot.farm.steam_news_bot.SteamNewsBot;
import bot.farm.steam_news_bot.dto.Message;
import bot.farm.steam_news_bot.entity.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class AnnouncementService {
  private final UserService userService;
  private final SteamNewsBot steamNewsBot;
  
  public AnnouncementService(UserService userService, SteamNewsBot steamNewsBot) {
    this.userService = userService;
    this.steamNewsBot = steamNewsBot;
  }
  
  public ResponseEntity<Map<String, String>> receiveMessageAndNotificationUsers(
      @Validated @RequestBody Message message) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    message.setDate(LocalDateTime.now().format(formatter));
    
    List<User> users = userService.getUsersByActive(true);
    users.forEach(user -> steamNewsBot.sendTextMessage(user.getChatId(), message.toString()));
    
    return ResponseEntity.ok(Map.of(
        "service", "message successfully received. " + users.size() + " users notified."));
  }
  
}
