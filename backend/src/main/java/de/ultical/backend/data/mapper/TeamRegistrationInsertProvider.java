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
