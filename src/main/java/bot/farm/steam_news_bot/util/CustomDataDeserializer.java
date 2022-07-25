package bot.farm.steam_news_bot.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CustomDataDeserializer extends StdDeserializer<String> {

    protected CustomDataDeserializer() {
        this(null);
    }

    protected CustomDataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        String value = jsonParser.getText();
        if (!value.isBlank()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(value)), ZoneId.systemDefault());
            return ldt.format(formatter);
        }
        return null;
    }

}
