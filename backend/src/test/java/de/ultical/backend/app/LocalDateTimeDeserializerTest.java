/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
