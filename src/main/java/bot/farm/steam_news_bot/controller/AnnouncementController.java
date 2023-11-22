package bot.farm.steam_news_bot.controller;

import bot.farm.steam_news_bot.dto.Message;
import bot.farm.steam_news_bot.service.AnnouncementService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller class for handling announcements API requests.
 * Provides endpoints related to receiving messages and sending notifications to users.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnnouncementController {

  private final AnnouncementService announcementService;

  /**
   * Endpoint for receiving messages and triggering notifications to users.
   * Accepts a JSON payload containing the message details.
   *
   * @param message the Message object received in the request body
   * @return a ResponseEntity containing a map with response data
   */
  @PostMapping("/receive")
  public ResponseEntity<Map<String, String>> receiveMessage(@RequestBody Message message) {
    return announcementService.receiveMessageAndNotificationUsers(message);
  }
}
