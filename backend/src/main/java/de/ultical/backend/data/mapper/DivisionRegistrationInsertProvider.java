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
package de.ultical.backend.data.mapper;

import java.util.Map;
import java.util.Objects;

import org.apache.ibatis.jdbc.SQL;

import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationPlayers;
import de.ultical.backend.model.TournamentEdition;

public class DivisionRegistrationInsertProvider {

    public static String getInsertSql(final Map<String, Object> params) {
        DivisionRegistration reg = (DivisionRegistration) Objects.requireNonNull(params.get("reg"));
        TournamentEdition edition = (TournamentEdition) Objects.requireNonNull(params.get("edition"));
        Objects.requireNonNull(edition);
        final boolean is_player_registration = reg instanceof DivisionRegistrationPlayers;
        SQL sql = new SQL();
        sql.INSERT_INTO("DIVISION_REGISTRATION");
        sql.VALUES("tournament_edition", String.valueOf(edition.getId()));
        sql.VALUES("division_type", "'" + reg.getDivisionType().name() + "'");
        sql.VALUES("division_age", "'" + reg.getDivisionAge().name() + "'");
        sql.VALUES("number_of_spots", String.valueOf(reg.getNumberSpots()));
        sql.VALUES("is_player_registration", Boolean.toString(is_player_registration));
        sql.VALUES("division_identifier", "'" + reg.getDivisionIdentifier() + "'");

        return sql.toString();
    }
}
