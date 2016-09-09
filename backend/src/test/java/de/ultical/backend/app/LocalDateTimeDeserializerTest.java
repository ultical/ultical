package de.ultical.backend.app;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LocalDateTimeDeserializerTest {

    private ObjectMapper mapper;

    private String[] inputs;

    private LocalDateTimeDeserializer deserializer = new LocalDateTimeDeserializer();

    @Before
    public void setUp() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.inputs = new String[2];
        this.inputs[0] = "\"2016-02-09T07:31:38+04:00\"";
        this.inputs[1] = "\"2016-02-09T07:31:38.133\"";
    }

    @Test
    public void testDeserialization() throws JsonParseException, IOException {

        for (String input : this.inputs) {
            JsonParser parser = this.mapper.getFactory().createParser(input);
            DeserializationContext ctxt = this.mapper.getDeserializationContext();

            LocalDateTime result = this.deserializer.deserialize(parser, ctxt);

            assertEquals(result.getYear(), 2016);
            assertEquals(result.getMonthValue(), 2);
            assertEquals(result.getDayOfMonth(), 9);
            assertEquals(result.getHour(), 7);
            assertEquals(result.getMinute(), 31);
            assertEquals(result.getSecond(), 38);
        }
    }

}
