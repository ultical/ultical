package de.ultical.backend.app;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.ultical.backend.model.DivisionConfirmation;
import de.ultical.backend.model.DivisionConfirmationTeams;
import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationTeams;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DivisionConfirmationDeserializer extends JsonDeserializer<List<DivisionConfirmation>> {

    @Override
    public List<DivisionConfirmation> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = p.getCodec();
        ArrayNode node = (ArrayNode) oc.readTree(p);

        List<DivisionConfirmation> divisionConfirmations = new ArrayList<>();

        Iterator<JsonNode> nodeIterator = node.iterator();
        while (nodeIterator.hasNext()) {
            JsonNode idNode = nodeIterator.next();
            DivisionConfirmation newDivisionConfirmation = new DivisionConfirmationTeams();
            DivisionRegistration divisionRegistration = new DivisionRegistrationTeams();
            divisionRegistration.setId(idNode.asInt());
            newDivisionConfirmation.setDivisionRegistration(divisionRegistration);
            newDivisionConfirmation.setIndividualAssignment(false);
            divisionConfirmations.add(newDivisionConfirmation);
        }

        return divisionConfirmations;
    }

}
