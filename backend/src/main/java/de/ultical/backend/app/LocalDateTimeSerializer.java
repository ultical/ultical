package de.ultical.backend.app;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    private static final char DATE_TIME_SEPARATOR = 'T';
    private static final char TIME_SEPARATOR = ':';
    private static final char DATE_SEPARATOR = '-';
    public final static DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
            .appendLiteral(DATE_SEPARATOR).appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral(DATE_SEPARATOR)
            .appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral(DATE_TIME_SEPARATOR)
            .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(TIME_SEPARATOR)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(TIME_SEPARATOR)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendLiteral('.').appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter();

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        if (value == null) {
            gen.writeNull();
        } else {
            try {
                final String dateAsString = value.format(dtf);
                gen.writeString(dateAsString);
            } catch (DateTimeException dte) {
                throw new JsonMappingException("Unable to format LocalDateTime to String", dte);
            }
        }
    }

}
