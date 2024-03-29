package bot.farm.snb.service;

import bot.farm.snb.SteamNewsBot;
import bot.farm.snb.dto.Message;
import bot.farm.snb.entity.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Service class for managing announcements and sending notifications to active users.
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {
  private final UserService userService;
  private final SteamNewsBot steamNewsBot;

  /**
   * Receives a message and sends notifications to active users.
   *
   * @param message The message to be sent.
   * @return A ResponseEntity with a success message and the number of users notified.
   */
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
