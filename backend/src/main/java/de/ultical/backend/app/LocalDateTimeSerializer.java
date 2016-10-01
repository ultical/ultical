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
