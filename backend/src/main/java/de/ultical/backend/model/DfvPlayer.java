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
package de.ultical.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.ultical.backend.api.transferClasses.DfvMvPlayer;
import de.ultical.backend.app.LocalDateDeserializer;
import de.ultical.backend.app.LocalDateSerializer;
import de.ultical.backend.app.LocalDateTimeDeserializer;
import de.ultical.backend.app.LocalDateTimeSerializer;
import de.ultical.backend.data.mapper.DfvPlayerMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DfvPlayer extends Player {

    public DfvPlayer() {
        super();
    }

    public DfvPlayer(DfvMvPlayer dfvPlayer) {
        super();
        this.setBirthDate(LocalDate.parse(dfvPlayer.getDobString()));
        this.setDfvNumber(dfvPlayer.getDfvNumber());
        this.setGender(Gender.robustValueOf(dfvPlayer.getGender()));
    }

    private int dfvNumber;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eligibleUntil;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthDate;

    private Club club;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastModified;

    /**
     * <code>True</code> if the player is eligible for tournaments of the
     * DFV.<br />
     * This method simply checks whether {@link #eligibleUntil} is null,
     * following the assumption that the <code>eligibleUntil</code> value always
     * a point in the past.
     * 
     * @return <code>true</code> if the player is eligible for DFV tournaments,
     *         <code>false</code> otherwise.
     */
    @JsonIgnore
    public boolean isEligible() {
        return this.eligibleUntil == null;
    }

    @Override
    public Class<DfvPlayerMapper> getMapper() {
        return DfvPlayerMapper.class;
    }
}
