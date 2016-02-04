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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import org.apache.ibatis.jdbc.SQL;

import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.TeamRegistration;

public class TeamRegistrationInsertProvider {

    public static String getInsertSql(Map<String, Object> params) {
        final DivisionRegistrationTeams div = (DivisionRegistrationTeams) Objects.requireNonNull(params.get("div"));
        final TeamRegistration teamReg = (TeamRegistration) Objects.requireNonNull(params.get("team"));
        String regTime = "'" + Timestamp.valueOf(LocalDateTime.now()).toString() + "'";
        // SQL sequenceSubSelect = new
        // SQL().SELECT("MAX(sequence)").FROM("TEAM_REGISTRATION")
        // .WHERE("division_registration=" + div.getId());
        SQL sql = new SQL();
        sql.INSERT_INTO("TEAM_REGISTRATION").VALUES("division_registration", String.valueOf(div.getId()))
                .VALUES("time_registered", regTime);
        if (teamReg.getComment() != null) {
            sql.VALUES("comment", "'" + teamReg.getComment() + "'");
        }
        sql.VALUES("team", String.valueOf(teamReg.getTeam().getId()));
        return sql.toString();
    }
}
