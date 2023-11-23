package bot.farm.snb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main entry point of the SteamNewsBot application.
 */
@SpringBootApplication
@EnableScheduling
public class SteamNewsBotApplication {

  public static void main(String[] args) {

    SpringApplication.run(SteamNewsBotApplication.class, args);
  }
}
