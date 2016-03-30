package de.ultical.backend.app;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = p.getCodec();
        TextNode node = (TextNode) oc.readTree(p);
        String dateString = node.textValue();
        try {
            return LocalDateTime.parse(dateString, LocalDateTimeSerializer.dtf);
        } catch (DateTimeParseException dtpe) {
            throw new JsonParseException("Unable to parse text as LocalDateTime", p.getCurrentLocation(), dtpe);
        }
    }

}
