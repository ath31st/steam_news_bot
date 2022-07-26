package bot.farm.steam_news_bot.controller;

import bot.farm.steam_news_bot.dto.Message;
import bot.farm.steam_news_bot.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping("/receive")
    public ResponseEntity<Map<String, String>> receiveMessage(@RequestBody Message message) {
        return announcementService.receiveMessageAndNotificationUsers(message);
    }
}
