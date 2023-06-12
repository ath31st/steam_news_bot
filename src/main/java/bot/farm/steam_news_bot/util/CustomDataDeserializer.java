package bot.farm.steam_news_bot.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Custom deserializer for converting a JSON string to a formatted date and time string.
 */
public class CustomDataDeserializer extends StdDeserializer<String> {
  
  protected CustomDataDeserializer() {
    this(null);
  }
  
  /**
   * Constructor with the specified value class.
   *
   * @param vc the value class for the deserializer.
   */
  
  protected CustomDataDeserializer(Class<?> vc) {
    super(vc);
  }
  
  /**
   * Deserializes the JSON string into a formatted date and time string.
   *
   * @param jsonParser the JSON parser used for reading the JSON string.
   * @param context    the deserialization context.
   * @return the formatted date and time string.
   * @throws IOException if an I/O error occurs during deserialization.
   */
  @Override
  public String deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException {
    String value = jsonParser.getText();
    if (value != null) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
      LocalDateTime ldt = LocalDateTime.ofInstant(
          Instant.ofEpochSecond(Long.parseLong(value)), ZoneId.systemDefault());
      return ldt.format(formatter);
    }
    return null;
  }
}
