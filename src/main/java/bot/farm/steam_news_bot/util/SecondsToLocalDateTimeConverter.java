package bot.farm.steam_news_bot.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Converter
public class SecondsToLocalDateTimeConverter implements AttributeConverter<LocalDateTime, Long> {
    @Override
    public Long convertToDatabaseColumn(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return localDateTime.toEpochSecond(ZoneOffset.MAX);
        }
        return null;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long aLong) {
        if (aLong != null) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(aLong), ZoneId.systemDefault());
        }
        return null;
    }
}
