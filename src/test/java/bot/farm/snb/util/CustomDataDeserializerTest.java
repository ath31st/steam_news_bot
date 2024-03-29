package bot.farm.snb.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomDataDeserializerTest {

  @Test
  void deserialize() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    DeserializationContext context = objectMapper.getDeserializationContext();
    CustomDataDeserializer customDataDeserializer = new CustomDataDeserializer();
    JsonParser jsonParser = objectMapper.createParser("1660147533");

    jsonParser.nextToken();
    assertEquals("10.08.2022 19:05", customDataDeserializer.deserialize(jsonParser, context));

    jsonParser = objectMapper.createParser("");
    jsonParser.nextToken();
    assertNull(customDataDeserializer.deserialize(jsonParser, context));
  }
}