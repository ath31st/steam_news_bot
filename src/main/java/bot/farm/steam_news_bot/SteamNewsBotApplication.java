package bot.farm.steam_news_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SteamNewsBotApplication {
  
  public static void main(String[] args) {
    
    SpringApplication.run(SteamNewsBotApplication.class, args);
  }
  
}
