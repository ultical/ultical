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

import java.security.Principal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.ultical.backend.data.mapper.UserMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends Identifiable implements Principal {

    private String email;
    private String password;
    private DfvPlayer dfvPlayer;
    private boolean emailConfirmed;
    private boolean dfvEmailOptIn;

    public String getFullName() {
        if (this.dfvPlayer != null) {
            return this.getDfvPlayer().getFirstName() + " " + this.getDfvPlayer().getLastName();
        } else {
            return this.getEmail();
        }
    }

    @Override
    public Class<UserMapper> getMapper() {
        return UserMapper.class;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return this.email;
    }
}
